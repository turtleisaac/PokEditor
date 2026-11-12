# Ratchet PIT scores. Timeouts count as killed.

import argparse
import json
import sys
import xml.etree.ElementTree as ET

from gatelib import changed_ranges, read_json, repo_root, write_json

EMPTY = {'killed': 0, 'timedOut': 0, 'survived': 0, 'noCoverage': 0, 'other': 0}
IGNORED = {'NONVIABLE', 'EQUIVALENT', 'COMPILEERROR', 'IGNORED'}
SURVIVOR_LIMIT = 40

def main(argv=None):
    """
    Compare the mutation reports to the baseline, or write a new baseline
    """
    parser = argparse.ArgumentParser(
        description='Ratchet PIT scores against .quality/mutation-baseline.json.')
    parser.add_argument('--root')
    parser.add_argument('--scope', choices=('changed', 'full'), default='full')
    parser.add_argument('--base', help='Git ref used to find changed files.')
    parser.add_argument('--write', action='store_true')
    args = parser.parse_args(argv)

    root = repo_root(args.root)
    try:
        java = load_pit(root)
        if args.write or args.scope == 'full':
            require_scored(java)

        if args.base:
            mode = 'base'
        else:
            mode = 'none'

        ranges = changed_ranges(root, mode, args.base)
    except (RuntimeError, ET.ParseError, json.JSONDecodeError) as exc:
        print(exc, file=sys.stderr)
        return 2

    if args.write:
        write_baseline(root, java)
        print_scores(java)
        return 0

    if not (root / '.quality' / 'mutation-baseline.json').is_file():
        print('missing .quality/mutation-baseline.json', file=sys.stderr)
        return 2

    failures, reports = judge(root, args.scope, java, ranges)
    for line in reports:
        print(line)

    if failures:
        print('%s mutation gate failure(s):' % len(failures))
        for failure in failures:
            print('  %s' % failure)
        return 1

    print('Mutation gate passed.')
    return 0

def load_pit(root):
    """
    Per-class PIT counts, and the mutants that survived
    """
    classes = {}
    survivors = []
    for report in sorted(root.glob('**/target/pit-reports/mutations.xml')):
        if '.upstream' in report.parts or '.quality' in report.parts:
            continue

        root_node = ET.parse(report).getroot()
        for node in root_node.findall('mutation'):
            add_pit(classes, survivors, node)

    return {'classes': classes, 'survivors': survivors, 'totals': sum_counts(classes)}

def add_pit(classes, survivors, node):
    """
    Count one PIT mutant, and keep a row for it when it survived
    """
    status = bucket((node.get('status') or '').upper())
    if status is None:
        return

    name = node.findtext('mutatedClass') or ''
    counts = classes.setdefault(name, dict(EMPTY))
    counts[status] += 1
    if status != 'survived':
        return

    survivors.append({
        'stack': 'java',
        'id': name,
        'method': node.findtext('mutatedMethod') or '',
        'line': node.findtext('lineNumber') or '',
        'mutator': (node.findtext('mutator') or '').rsplit('.', 1)[-1],
        'description': node.findtext('description') or '',
    })

def bucket(status):
    """
    Score bucket for a tool status, or None when that mutant is not scored
    """
    token = status.replace('-', '').replace('_', '')
    if token == 'KILLED':
        return 'killed'
    if token in ('TIMEDOUT', 'TIMEOUT'):
        return 'timedOut'
    if token == 'SURVIVED':
        return 'survived'
    if token == 'NOCOVERAGE':
        return 'noCoverage'
    if token in IGNORED or not token:
        return None

    return 'other'

def sum_counts(groups):
    """
    Add per-class or per-file counts into one total
    """
    total = dict(EMPTY)
    for counts in groups.values():
        for key in total:
            total[key] += counts.get(key, 0)

    return total

def hits(counts):
    """
    Mutants the tests detected. Timeouts count as killed
    """
    return counts.get('killed', 0) + counts.get('timedOut', 0)

def total_of(counts):
    """
    Scored mutants. Ignored statuses are already excluded
    """
    return sum(counts.get(key, 0) for key in EMPTY)

def require_scored(java):
    """
    Fail when PIT scored no mutants
    """
    if total_of(java['totals']) == 0:
        raise RuntimeError('PIT report has no scored mutants')

def score_of(counts):
    """
    Detected divided by scored, or None when there is nothing to score
    """
    total = total_of(counts)
    if total == 0:
        return None
    return hits(counts) / total

def dropped(new, old):
    """
    True when the new ratio is lower than the old one
    """
    new_total = total_of(new)
    old_total = total_of(old)
    if old_total == 0:
        return False
    if new_total == 0:
        return True

    return hits(new) * old_total < hits(old) * new_total

def percent(counts):
    """
    The score as a percentage, or n/a when there is nothing to score
    """
    score = score_of(counts)
    if score is None:
        return 'n/a'
    return '%.2f%%' % (score * 100)

def write_baseline(root, java):
    """
    Store the totals and the per-class counts
    """
    payload = {
        'version': 1,
        'score': {
            'killedAndTimedOut': 'counted as detected',
            'excluded': ['NON_VIABLE', 'EQUIVALENT', 'COMPILE_ERROR', 'IGNORED'],
        },
        'java': {'tool': 'pitest', 'totals': java['totals'], 'classes': java['classes']},
    }
    write_json(root / '.quality' / 'mutation-baseline.json', payload)

def print_scores(java):
    """
    Print the Java mutation score
    """
    counts = java['totals']
    print('java mutation score %s (killed %s / %s)' %
          (percent(counts), hits(counts), total_of(counts)))

def judge(root, scope, java, ranges):
    """
    Failures and log lines for a full run or a changed-file run
    """
    baseline = read_json(root / '.quality' / 'mutation-baseline.json')
    if scope == 'full':
        return judge_full(baseline, java)
    return judge_changed(baseline, java, ranges)

def judge_full(baseline, java):
    """
    The stack score, and every known class, may not drop
    """
    failures = []
    failures.extend(stack_drop('java', java['totals'], baseline['java']['totals']))
    failures.extend(group_drop('java class', java['classes'], baseline['java'].get('classes', {})))

    lines = ['Full mutation run']
    lines.append('java %s (%s/%s)' % (percent(java['totals']), hits(java['totals']), total_of(java['totals'])))
    lines.extend(survivor_lines(java['survivors'], None))

    return failures, lines

def judge_changed(baseline, java, ranges):
    """
    Changed classes may not drop. Their surviving mutants are listed
    """
    java_ids = changed_classes(ranges)
    if not java_ids:
        return [], ['No changed Java to mutate.']

    failures = changed_group(
        'java class', java_ids, java['classes'], baseline['java'].get('classes', {}))
    lines = ['Incremental mutation run']
    lines.extend(survivor_lines(java['survivors'], set(java_ids)))

    return failures, lines

def changed_classes(ranges):
    """
    Outer class names for changed main-source Java files
    """
    found = []
    marker = 'src/main/java/'
    for path in sorted(ranges):
        if marker not in path or not path.endswith('.java'):
            continue
        qualified = path.split(marker, 1)[1][:-5].replace('/', '.')
        found.append(qualified)

    return found

def stack_drop(label, new, old):
    """
    A failure line when a whole-stack score fell
    """
    if not dropped(new, old):
        return []
    return ['%s mutation score dropped: %s -> %s' % (label, percent(old), percent(new))]

def group_drop(label, new_groups, old_groups):
    """
    A failure line for each known class or file whose score fell
    """
    failures = []
    for name, old in sorted(old_groups.items()):
        if name not in new_groups:
            continue
        if dropped(new_groups[name], old):
            failures.append('%s %s mutation score dropped: %s -> %s' %
                            (label, name, percent(old), percent(new_groups[name])))

    return failures

def changed_group(label, names, new_groups, old_groups):
    """
    Known names may not score lower. A new file with no result is skipped
    """
    failures = []
    for name in names:
        matches = matching_keys(name, new_groups)
        if not matches:
            if name not in old_groups:
                continue
            failures.append('no mutation result for %s %s' % (label, name))
            continue

        for key in matches:
            old = old_groups.get(key)
            if old is None and key != name:
                old = old_groups.get(name)
            if old is None:
                continue

            if dropped(new_groups[key], old):
                failures.append('%s %s mutation score dropped: %s -> %s' %
                                (label, key, percent(old), percent(new_groups[key])))

    return failures

def matching_keys(name, groups):
    """
    The class itself and its PIT inner classes, named Outer$Inner
    """
    exact = []
    if name in groups:
        exact.append(name)

    nested = [key for key in groups if key.startswith(name + '$')]
    return exact + nested

def survivor_lines(survivors, watched):
    """
    Log lines for surviving mutants. watched limits them to those ids
    """
    chosen = []
    for row in survivors:
        if watched is not None and not is_watched(row['id'], watched):
            continue
        chosen.append(row)

    lines = ['Surviving mutants: %s' % len(chosen)]
    for row in chosen[:SURVIVOR_LIMIT]:
        where = row['method'] or row['id']
        lines.append('  SURVIVED %s %s:%s %s %s' %
                     (row['stack'], where, row['line'], row['mutator'], row['description']))

    if len(chosen) > SURVIVOR_LIMIT:
        lines.append('  ... %s more' % (len(chosen) - SURVIVOR_LIMIT))
    return lines

def is_watched(identifier, watched):
    """
    True when identifier is a changed name, or an inner class of one
    """
    if identifier in watched:
        return True
    return any(identifier.startswith(name + '$') for name in watched)

if __name__ == '__main__':
    sys.exit(main())
