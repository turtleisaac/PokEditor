# Shared pieces for the crapper, dryer, and mutation ratchets.

import hashlib
import json
import re
import subprocess
from pathlib import Path

CRAP_LIMIT = 10.0
CRAP_EPSILON = 0.001
SOURCE_ROOTS = ('src/main/java',)
# Build output and a sibling checkout are not this repo's source.
SKIP_PARTS = {'target', 'node_modules', 'dist', 'coverage', '.upstream', '.quality'}
HUNK = re.compile(r'^@@ -(\d+)(?:,(\d+))? \+(\d+)(?:,(\d+))? @@')

def repo_root(explicit=None):
    """
    Checkout to score. explicit is that path, or this file's repo when it is omitted
    """
    if explicit:
        return Path(explicit).resolve()
    return Path(__file__).resolve().parents[2]

def read_pins(root):
    """
    The pinned commit for each tool, read from requirements.txt
    """
    found = {}
    text = (root / '.quality' / 'requirements.txt').read_text(encoding='utf-8')
    for raw in text.splitlines():
        line = raw.split('#', 1)[0].strip()
        if '@' not in line or 'git+' not in line:
            continue
        name, url = line.split('@', 1)
        found[name.strip()] = url.strip().rsplit('@', 1)[-1]

    return found

def git_text(root, args):
    """
    Run git in root and return its stdout
    """
    result = subprocess.run(
        ['git', '-C', str(root), *args],
        check=False,
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        message = result.stderr.strip() or 'git failed'
        raise RuntimeError(message)
    return result.stdout

def parse_diff(text):
    """
    Map each path in a unified diff to the new-file line ranges it touches
    """
    # path -> inclusive new-file line ranges
    files = {}
    current = None
    for line in text.splitlines():
        if line.startswith('+++ '):
            current = diff_path(line[4:])
            if current is not None:
                files.setdefault(current, [])
            continue

        match = HUNK.match(line)
        if match is None or current is None:
            continue

        start = int(match.group(3))
        count = int(match.group(4) or '1')
        if count <= 0:
            continue

        files[current].append((start, start + count - 1))

    return files

def diff_path(raw):
    """
    Strip git's b/ prefix. /dev/null means the file was deleted
    """
    path = raw.strip()
    if path == '/dev/null':
        return None
    if path.startswith('b/'):
        path = path[2:]
    if path.startswith('"') and path.endswith('"'):
        path = path[1:-1]

    return path

def changed_ranges(root, mode, base):
    """
    Line ranges treated as edited. mode is staged, base, or none
    """
    # staged is the index. base is the merge diff plus a dirty tree.
    if mode == 'none':
        return {}
    if mode == 'staged':
        return parse_diff(git_text(root, ['diff', '--cached', '-U0', '--no-color']))
    if not base:
        raise RuntimeError('--base requires a git ref')

    parts = [
        git_text(root, ['diff', '-U0', '--no-color', '%s...HEAD' % base]),
        git_text(root, ['diff', '-U0', '--no-color', 'HEAD']),
        git_text(root, ['diff', '--cached', '-U0', '--no-color']),
    ]

    return parse_diff(''.join(parts))

def overlaps(path, start, end, ranges):
    """
    True when start through end shares a line with a hunk in path
    """
    for hunk_start, hunk_end in ranges.get(path, []):
        if start <= hunk_end and hunk_start <= end:
            return True

    return False

def body_hash(text):
    """
    Hash of the text with whitespace removed, so a reformat stays the same function
    """
    collapsed = ''.join(text.split())
    return hashlib.sha256(collapsed.encode('utf-8')).hexdigest()[:16]

def snippet(path, start, end):
    """
    Source lines from start through end, both inclusive
    """
    lines = path.read_text(encoding='utf-8', errors='replace').splitlines()
    return '\n'.join(lines[start - 1:end])

def write_json(path, payload):
    """
    Write payload as indented JSON with a trailing newline
    """
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + '\n', encoding='utf-8')

def read_json(path):
    """
    Load a JSON file
    """
    return json.loads(path.read_text(encoding='utf-8'))

def jacoco_reports(root):
    """
    JaCoCo XML at the repo root and in a module subdirectory
    """
    found = list(root.glob('target/site/jacoco/jacoco.xml'))
    found.extend(root.glob('*/target/site/jacoco/jacoco.xml'))
    return found

def require_coverage(root):
    """
    Raise when the JaCoCo XML report is missing
    """
    if not jacoco_reports(root):
        raise RuntimeError('JaCoCo XML is missing. Run scripts/quality/coverage.sh')

def generated_method(row):
    """
    JFormDesigner writes initComponents. That method is not hand-written
    """
    return row['name'] == 'initComponents'

def generated_spans(rows):
    """
    Line ranges of initComponents, including anything nested inside it
    """
    spans = {}
    for row in rows:
        if not generated_method(row):
            continue
        spans.setdefault(row['path'], []).append((row['start'], row['end']))

    return spans

def inside_span(path, start, end, spans):
    """
    True when start through end sits inside a generated range
    """
    for span_start, span_end in spans.get(path, []):
        if span_start <= start and end <= span_end:
            return True

    return False

def collect_functions(root):
    """
    Score every Java function with crapper, leaving out generated initComponents
    """
    from crapper.cli import parse_args, select_files
    from crapper.coverage import load_bundle
    from crapper.crap import crap_score, round_metric
    from crapper.discover import language_of
    from crapper.languages import functions_in_file

    argv = ['--root', str(root), '--use-existing-coverage']
    for source_root in SOURCE_ROOTS:
        argv.extend(['--source-root', source_root])

    files = [path for path in select_files(parse_args(argv)) if included(path, root)]
    relatives = [relative(path, root) for path in files]
    bundle = load_bundle(root)
    bundle.bind_sources(relatives)

    rows = []
    for path, rel in zip(files, relatives):
        language = language_of(path)
        source = path.read_text(encoding='utf-8', errors='replace')
        for function in functions_in_file(language, source, rel, str(root)):
            rows.append(score_function(root, function, bundle, round_metric, crap_score))

    rows.sort(key=lambda row: (row['path'], row['namespace'], row['name'], row['start']))
    spans = generated_spans(rows)

    kept = []
    for row in rows:
        if inside_span(row['path'], row['start'], row['end'], spans):
            continue
        kept.append(row)

    return kept, spans

def included(path, root):
    """
    False when the path sits in a build tree or a sibling checkout
    """
    return not any(part in SKIP_PARTS for part in Path(relative(path, root)).parts)

def relative(path, root):
    """
    Path relative to root, or the absolute path when it is outside the checkout
    """
    try:
        return path.resolve().relative_to(root.resolve()).as_posix()
    except ValueError:
        return path.resolve().as_posix()

def score_function(root, function, bundle, round_metric, crap_score):
    """
    One crapper row, plus a whitespace-insensitive hash of the body
    """
    coverage = bundle.percent_for(function)
    if coverage is None:
        coverage = 0.0
    coverage = round_metric(coverage)
    score = round_metric(crap_score(function.complexity, coverage))
    text = snippet(root / function.path, function.start_line, function.end_line)

    return {
        'path': function.path,
        'namespace': function.namespace,
        'name': function.name,
        'language': function.language,
        'start': function.start_line,
        'end': function.end_line,
        'complexity': function.complexity,
        'coverage': coverage,
        'crap': score,
        'body': body_hash(text),
    }

def generated_pair(pair, spans):
    """
    True when either side sits in a JFormDesigner initComponents block
    """
    for side in (pair['left'], pair['right']):
        if inside_span(side['path'], side['start'], side['end'], spans):
            return True

    return False

def collect_pairs(root, functions, spans):
    """
    Dryer pairs at the tool's default threshold, each with a stable identity
    """
    from dryer.cli import parse_args, select_files
    from dryer.scan import find_duplicates, scan_files

    argv = ['--root', str(root)]
    for source_root in SOURCE_ROOTS:
        argv.extend(['--source-root', source_root])

    options = parse_args(argv)
    files = [path for path in select_files(options) if included(path, root)]
    entries, warnings = scan_files(files, root, options.min_lines, options.min_nodes)
    for warning in warnings:
        print('dryer: %s' % warning)

    pairs = find_duplicates(entries, options.threshold)
    rows = []
    for pair in pairs:
        row = pair_row(root, pair, functions)
        if generated_pair(row, spans):
            continue
        rows.append(row)

    rows.sort(key=lambda row: (-row['score'], row['left']['path'], row['left']['start']))

    return rows, options.threshold

def pair_row(root, pair, functions):
    """
    One dryer candidate, keyed by the two functions rather than by line number
    """
    return {
        'language': pair.language,
        'score': round(pair.score, 6),
        'left': pair_side(root, pair.left, functions),
        'right': pair_side(root, pair.right, functions),
    }

def pair_side(root, span, functions):
    """
    Identity of one side of a pair. A span with no function falls back to a hash of its text
    """
    owner = function_at(functions, span.file, span.start_line, span.end_line)
    if owner is not None:
        return {
            'path': owner['path'],
            'namespace': owner['namespace'],
            'name': owner['name'],
            'body': owner['body'],
            'start': span.start_line,
            'end': span.end_line,
        }

    text = snippet(root / span.file, span.start_line, span.end_line)
    return {
        'path': span.file,
        'namespace': '',
        'name': '',
        'body': body_hash(text),
        'start': span.start_line,
        'end': span.end_line,
    }

def function_at(functions, path, start, end):
    """
    The crapper function that contains this span, if there is one
    """
    containing = [
        row
        for row in functions
        if row['path'] == path and row['start'] <= start and end <= row['end']
    ]
    if containing:
        return min(containing, key=lambda row: row['end'] - row['start'])

    overlapping = [
        row
        for row in functions
        if row['path'] == path and row['start'] <= end and start <= row['end']
    ]
    if not overlapping:
        return None
    return min(overlapping, key=lambda row: row['end'] - row['start'])

def pair_key(pair):
    """
    Order-independent pair identity. Line numbers are left out
    """
    sides = []
    for side in (pair['left'], pair['right']):
        sides.append((side['path'], side['namespace'], side['name'], side['body']))

    return tuple(sorted(sides))

def group_functions(rows):
    """
    Group overloads that share a path, namespace, and name
    """
    groups = {}
    for row in rows:
        key = (row['path'], row['namespace'], row['name'])
        groups.setdefault(key, []).append(row)

    return groups

def match_overloads(baseline, current):
    """
    Match functions to the baseline by body, then by nearest line
    """
    unused = list(baseline)
    matched = []
    pending = []
    for row in current:
        found = None
        for index, old in enumerate(unused):
            if old['body'] == row['body']:
                found = unused.pop(index)
                break

        if found is None:
            pending.append(row)
        else:
            matched.append((found, row))

    fresh = []
    for row in pending:
        if not unused:
            fresh.append(row)
            continue

        found = min(unused, key=lambda item: abs(item['start'] - row['start']))
        unused.remove(found)
        matched.append((found, row))

    return matched, fresh

def format_function(row):
    """
    One-line label for a scored function
    """
    return '%s.%s (%s:%s)' % (row['namespace'], row['name'], row['path'], row['start'])

def format_side(side):
    """
    One-line label for one side of a duplicate pair
    """
    name = side['name'] or '<form>'
    return '%s:%s-%s %s' % (side['path'], side['start'], side['end'], name)
