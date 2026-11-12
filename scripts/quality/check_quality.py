# Ratchet crapper CRAP scores and dryer duplicate pairs against .quality/.

import argparse
import sys

from gatelib import (
    CRAP_EPSILON,
    CRAP_LIMIT,
    changed_ranges,
    collect_functions,
    collect_pairs,
    format_function,
    format_side,
    group_functions,
    match_overloads,
    overlaps,
    pair_key,
    read_json,
    read_pins,
    repo_root,
    require_coverage,
    write_json,
)

def main(argv=None):
    """
    Compare the tree to the baselines, or rewrite them when --write is set
    """
    parser = argparse.ArgumentParser(
        description='Ratchet crapper and dryer against .quality/.')
    parser.add_argument('--root', help='PokEditor checkout. Defaults to the repo root.')
    parser.add_argument('--base', help='Git ref changed functions are diffed against.')
    parser.add_argument('--staged', action='store_true', help='Diff the index, for pre-commit.')
    parser.add_argument('--write', action='store_true', help='Replace the CRAP and dryer baselines.')
    args = parser.parse_args(argv)

    root = repo_root(args.root)
    try:
        require_coverage(root)
        if args.staged:
            mode = 'staged'
        elif args.base:
            mode = 'base'
        else:
            mode = 'none'

        ranges = changed_ranges(root, mode, args.base)
        functions, spans = collect_functions(root)
        pairs, threshold = collect_pairs(root, functions, spans)
    except RuntimeError as exc:
        print(exc, file=sys.stderr)
        return 2

    pins = read_pins(root)
    if args.write:
        write_baselines(root, pins, functions, pairs, threshold)
        print_summary(functions, pairs)
        return 0

    failures = crap_failures(root, pins, functions, ranges)
    failures.extend(dry_failures(root, pins, pairs, threshold, ranges))

    if failures:
        print('%s quality gate failure(s):' % len(failures))
        for failure in failures:
            print('  %s' % failure)
        return 1

    print('Quality gate passed (%s functions, %s duplicate pairs at dryer threshold %s).' %
          (len(functions), len(pairs), threshold))
    return 0

def write_baselines(root, pins, functions, pairs, threshold):
    """
    Replace the CRAP and dryer baseline files
    """
    quality = root / '.quality'
    write_json(
        quality / 'crap-baseline.json',
        {'version': 1, 'crapper': pins['crapper'], 'functions': functions},
    )
    write_json(
        quality / 'dry-baseline.json',
        {
            'version': 1,
            'dryer': pins['dryer'],
            'threshold': threshold,
            'pairs': pairs,
        },
    )

def print_summary(functions, pairs):
    """
    Print the worst CRAP scores and the duplicate-pair count
    """
    ranked = sorted(functions, key=lambda row: (-(row['crap'] or 0), row['path'], row['name']))
    print('Worst CRAP scores:')
    for row in ranked[:15]:
        print('  %8.1f  cc=%-4s cov=%6.1f%%  %s' %
              (row['crap'], row['complexity'], row['coverage'], format_function(row)))

    print('Duplicate pairs: %s' % len(pairs))

def pin_failure(tool, recorded, current):
    """
    A message when the baseline was recorded with a different tool commit
    """
    if recorded == current:
        return []
    return ['%s pin %s does not match baseline pin %s' % (tool, current, recorded)]

def crap_failures(root, pins, functions, ranges):
    """
    Existing scores may not rise. A new or edited function must stay under CRAP 10
    """
    path = root / '.quality' / 'crap-baseline.json'
    if not path.is_file():
        return ['missing .quality/crap-baseline.json (run scripts/quality/refresh-baseline.sh)']

    baseline = read_json(path)
    failures = pin_failure('crapper', baseline.get('crapper'), pins.get('crapper'))
    if failures:
        return failures

    current = group_functions(functions)
    recorded = group_functions(baseline['functions'])

    for key, rows in current.items():
        matched, fresh = match_overloads(recorded.get(key, []), rows)
        for previous, row in matched:
            if row['crap'] > previous['crap'] + CRAP_EPSILON:
                failures.append('CRAP increased for %s: %s -> %s' %
                                (format_function(row), previous['crap'], row['crap']))
            edited = overlaps(row['path'], row['start'], row['end'], ranges)
            if edited and row['crap'] >= CRAP_LIMIT:
                failures.append('changed function %s has CRAP %s (ub-mode requires < %g)' %
                                (format_function(row), row['crap'], CRAP_LIMIT))

        for row in fresh:
            if row['crap'] >= CRAP_LIMIT:
                failures.append('new function %s has CRAP %s (ub-mode requires < %g)' %
                                (format_function(row), row['crap'], CRAP_LIMIT))

    return failures

def dry_failures(root, pins, pairs, threshold, ranges):
    """
    A new pair at the default threshold fails, and an edited function may not stay in one
    """
    path = root / '.quality' / 'dry-baseline.json'
    if not path.is_file():
        return ['missing .quality/dry-baseline.json (run scripts/quality/refresh-baseline.sh)']

    baseline = read_json(path)
    failures = pin_failure('dryer', baseline.get('dryer'), pins.get('dryer'))
    if abs(float(baseline['threshold']) - float(threshold)) > CRAP_EPSILON:
        failures.append('dryer threshold is %s, baseline recorded %s' %
                        (threshold, baseline['threshold']))

    known = {pair_key(pair) for pair in baseline['pairs']}

    for pair in pairs:
        edited = False
        for side in (pair['left'], pair['right']):
            if overlaps(side['path'], side['start'], side['end'], ranges):
                edited = True

        if pair_key(pair) in known and not edited:
            continue

        if pair_key(pair) not in known:
            kind = 'new duplicate pair'
        else:
            kind = 'changed duplicate pair'

        failures.append('%s score=%.2f %s <> %s' %
                        (kind, pair['score'], format_side(pair['left']), format_side(pair['right'])))

    return failures

if __name__ == '__main__':
    sys.exit(main())
