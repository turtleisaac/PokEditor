# Known defects and technical debt

Everything here was found during a bug-hunting pass across PokEditor and its three
supporting libraries, and everything here was **reproduced before being written down** —
either by running it or by reading the code path end to end. Where an item is a judgement
call rather than a defect, that is said.

Items are grouped by where they live and ordered by how much damage they do. Each says
what breaks, what it costs, and — where the answer is not obvious — what makes it awkward
to fix.

A `@Tag("dead-code")` test exists for several of these. Those run in a separate CI job that
is expected to fail, with the count asserted, so that a fix or a regression both show up.

---

## PokEditor

### Row add/delete is unsound on species-indexed sheets
**Severity: high. Reachable in one click.**

Personal, TM compatibility, Evolutions and Learnsets are parallel tables indexed by species
ID. Deleting a row renumbers every entry after it, so anything referring to those entries
by index — evolutions, learnsets, encounters, trainers — now points at the wrong species.

The cross-sheet half of this is fixed: the sheets share one species-name bank, and a
deletion on one used to shift the names on the others silently, which the next save wrote
to the ROM. The other sheets are now told, so the display no longer lies.

What remains is the operation itself. Either the row buttons should be removed from these
sheets, or "delete row" should mean "delete this species everywhere", which is a
coordinated change across four sheets and the name bank. Until then a deletion is a
renumbering the user is unlikely to be expecting.

### jide-oss reaches into encapsulated JDK packages, and breaks differently on each platform
**Severity: was high on Windows - one keystroke, every user. Fixed in packaging; the hazard
remains.**

`LookAndFeelFactory` chooses a style with
`lnf instanceof com.sun.java.swing.plaf.windows.WindowsLookAndFeel`, on the branch taken for
every look and feel it does not recognise - which includes FlatLaf, the one this application
sets. Every JIDE component runs it: `JidePopup.updateUI()` calls `installJideExtension()`, and
`updateUI()` runs from the JComponent constructor. This application arrives there whenever
someone types into a combo box in a sheet, because `EditorComboBox` installs a
`ComboBoxSearchable` whose search popup is a `JidePopup`.

An `instanceof` resolves its class before it can answer false, so the platform decides the
failure:

| | `com.sun.java.swing.plaf.windows` in `java.desktop`? | failure | what fixes it |
|---|---|---|---|
| Windows | yes, not exported | `IllegalAccessError` | `Add-Exports` in the jar manifest |
| Linux, macOS | no | `NoClassDefFoundError` | `WinLaF.jar` on the classpath |

**Neither fixes the other.** On Windows, parent-first delegation finds `java.desktop`'s copy and
shadows `WinLaF.jar` entirely, which is why the classpath copy that has always been there never
helped; `Add-Exports` naming a package a module does not have is ignored silently, which is why
carrying it on every platform is free. Both were reproduced and both fixes verified, including
against a `--patch-module` simulation of the Windows condition.

The `dist` profile now writes that manifest entry, and `JideLookAndFeelResolutionTest` guards
the classpath half - it fails if `WinLaF.jar` is dropped as apparent dead weight, which nothing
else would catch, since nothing names the class.

What remains is the dependency itself. jide-oss 3.6.18 is from 2015, predates the module system,
and also reaches `sun.awt`, `sun.awt.windows`, `sun.awt.image`, `sun.awt.shell` and
`com.apple.laf`; those are exported pre-emptively so the next one reached is not a second bug
report. It is used for exactly one class, `ComboBoxSearchable`, to give combo boxes type-to-
search. Replacing that one behaviour would remove a 2MB dependency, the `WinLaF.jar`
system-scoped hack, and this entire class of failure. That is the real fix and it is not done.

Note also that the version detection is stale: JIDE knows `os.version` 6.0, 6.1 and 6.2, so on
Windows 10 and 11 its own `isWindowsVistaAbove()` returns false. The crash arrives through the
`XPUtils` branch instead. Any workaround keyed on JIDE's idea of the OS would miss.

### `VariableTracker` is not published, so the project cannot be built from a clean checkout
**Severity: high for contributors, none for users.**

`pom.xml` declares `io.github.turtleisaac:VariableTracker:1.0-SNAPSHOT`, which exists in no
repository the build can reach and has no `<repositories>` entry. CI cannot build this
module, and neither can a new contributor. The field script editor is disabled, but
`ScriptDocument` still imports the library, so the dependency is still required to compile.

Fix by publishing VariableTracker, adding a repository that serves it, or vendoring it.

### The field script editor is disabled
**Deliberate, not a defect.** It is the one editor that compiles scripts back into the ROM,
so a half-working version damages a project rather than merely disappointing. Its
construction is commented out in `PokeditorManager`; the subsystem and its 202 tests are
intact.

### `CheckBoxEditor` turns anything that is not a Boolean into `false`
**Latent, not reachable today. Recorded because what keeps it unreachable is not obvious.**

It holds no copy of the value it was opened with, so `getCellEditorValue()` reports the
checkbox's state whatever arrived: null, an Integer and a String all commit as `false`. Opening a
cell would change it - the same defect fixed in the combo box and numeric editors.

Nothing reaches it. Every live CHECKBOX column reads an element of a `boolean[]` - the eight
Moves flags, the whole TM compatibility grid, Personal's FLIP - and the switch that serves them
covers every enum constant that maps to CHECKBOX, so the `return null` fallback below it is
unreachable for those columns.

The part worth writing down is TM compatibility, where `getCellType` returns `CHECKBOX` for
**every** column, including the two frozen ones. The species-name column is a String with a
checkbox editor declared for it. That is harmless only because the frozen model overrides
`isCellEditable` to false - so a change making a frozen column editable would write `false` over
a species name, and nothing in the type declarations would suggest why.

Not fixed rather than fixed speculatively: preserving the original value would hand null to a
write path that casts it to Boolean, so the fix is to stop the unconditional `getCellType`
claiming a type the column does not have, not to teach the editor to pass a value through.

### `jokes.txt` has never been committed
`Main` reads `/pokeditor/jokes.txt`, which is in no commit on any branch. Startup no longer
dies without it, but the start screen shows nothing. The file is the project owner's
content and has not been invented.

### Two sheets share one data list with independent models
Personal and TM compatibility are handed the same `List<PersonalData>`. Row changes on one
leave the other's *selection model* holding an index the data no longer has, so a
subsequent `getValueAt` on that row throws. The row count itself is fine — `getRowCount`
delegates live to the model.

Narrower than the name-bank problem above and largely self-healing on a repaint, but it is
the same shape: shared state, independent views.

### The save confirmation does not name every file it writes
The dialog lists the files from the parser's declared requirements, which for Personal is
the personal NARC only. The same save also writes arm9, because preparing the data mutates
it. The dialog promises a complete list and omits the one file whose corruption is hardest
to recover from.

### Reloading a sheet does not restore the name bank
`resetData` re-reads one data class. A row added or deleted beforehand has already changed
the shared `TextBankData`, which is not reloaded — so reload does not undo the edit.

Fixing it needs care: `TextBankData` is one dirty flag for the whole application, so
reloading it wholesale would discard unsaved move, item and ability name edits made from
other sheets, silently, and then mark everything clean.

### Dead code with no callers
`CsvReader`, `ArrayProcessor`, `XmlReader`, `BitStream`, `JarClassLoader`, `BitVector`,
`Directory`, `SheetExceptionFactory`, `JCheckboxTree`, `CircleButton` have no references in
`src/main`. They carry real defects — `XmlReader` tests for a close tag spelled with a
backslash and so cannot read well-formed XML at all; `ArrayProcessor` drops trailing empty
fields; `BitStream(0)` cannot grow — but nothing can reach them. Their tests are tagged
`dead-code`.

The decision to leave them as-is is deliberate. The open question is whether to delete them
or revive them, not whether to fix them in place.

### `DataManager` caches are process-wide with no reset
Static maps with no clear hook, which is why testing them needs reflection. They are scoped
to the ROM they were filled from, but nothing in the application can open a second ROM in
one session — the three menu entries that would are unimplemented and closing the tool
frame exits the process. The scoping is a guard against a future capability.

---

## Nds4j — **this library is published to Maven Central, where a released version is permanent**

### A scanned sprite is destroyed on save if its top-left pixel is not index 0
**Severity: high. Live in the product.**

The scanned NCGR decoder derives its decryption key from the first word of the *ciphertext*,
so a save/load round trip is only self-consistent when the first word of the *plaintext* is
zero. Edit the top-left pixel of a battle sprite, save, reload, and every row differs.

Reachable through the sprite editor's import and its left/right swap. No format
compatibility cost to fixing it, so it can be fixed in a patch release — but it destroys
user work today.

### The scanned NCGR path has no coverage that runs in CI
Every test touching a scanned image needs a retail ROM and skips without one. That is how an
8bpp under-allocation survived indefinitely and a 4bpp regression was introduced on top of
it with nothing going red.

Worse, the one scanned test that exists is a re-parse comparison, and a re-parse **cannot**
detect a wrong decryption key: `save()` re-encrypts from whatever key the decode returned,
so seed and direction errors cancel exactly and produce garbage pixels with a byte-identical
round trip. Verified by experiment.

A workflow appeared to cover this and never did. `maven-verify.yml` downloaded a ROM and ran the
full suite, but it moved the file into the workspace *before* `actions/checkout`, which cleans
untracked files and deleted it - and it ran on JDK 8, which cannot compile test sources using
`java.util.HexFormat`. Two independent faults, so the ROM suite has never run in CI here at all.
It obtained the ROM by reconstructing it with `xdelta3` against an empty source, meaning the
archive it fetched held the entire commercial game; that step is gone, and the workflow now takes
`-Drom.dir` so a runner with a legally obtained copy can point at one. Hosted runners have none,
so this gap stays open until such a runner exists or the fixture below does.

The fix is a committed synthetic fixture with expected pixel values derived from an
independent implementation — DSPRE's decoder is separately written and agrees — asserting
decoded pixels rather than the round trip.

### Cell banks of type 0 render at 8x8 and paste out of bounds
Type 0 NCER banks carry no bounding rectangle, so the cell image is sized from zeroes and
OAMs are pasted at raw, often negative, coordinates.

The fix is to derive the bounds from the OAMs themselves. Two constraints make it less
obvious than it looks: the derived bounds have to reach `CellImage.save()`, which reads the
`Cell` fields directly, so keeping them local just moves the crash to the save path; and
writing them back is byte-neutral **only** for type 0, because the writer omits those fields
for that type. Triggering on a degenerate rectangle instead of on the bank type would
silently rewrite a stored field on a type 1 bank.

A cell with no OAMs at all also needs a fallback — min and max over an empty set are
undefined.

### Scanned 8bpp images can be read but never written
`save()` throws for scanned 8bpp. Anything round-tripping a NARC containing one loses it.
Shipping a read-only depth is a permanent asymmetry in the public API, and it cannot be
removed later without a behaviour change.

The commented-out encoder appears correct — transcribed and run against the live decoder, it
reproduces the original ciphertext byte for byte in both scan directions. It needs its
buffer sized from the tile count rather than the pixel dimensions, as the 4bpp path now is.

### `getNcerImage` uses a different placement convention from the rest of the class
A hardcoded 80x80 canvas with a centre origin, carrying a `//todo undo this being 80`. It
matches Tinke's renderer, so it is inconsistent rather than wrong, but two conventions now
coexist in one class.

### `MemBuf` changed several published contracts in one release
`readByte()` returns unsigned where it returned signed; `readString` decodes ISO-8859-1
where it decoded UTF-8; `writeString(s, len)` truncates where it threw; `skip` rejects a
negative count; `align` no longer pads an already-aligned buffer.

Every in-tree caller normalises explicitly and so is unaffected, but these are behavioural
changes to a published surface and belong in the release notes. `align` is
format-visible — NARCs written after the change are not byte-identical to those written
before.

Worth considering before the version is permanent: `readByte()` is now a duplicate of
`readUInt8()`, no signed byte reader survives, and the name contradicts
`DataInput.readByte()`. A deprecation and a clearly-named pair would cost nothing now and
cannot be done cheaply later.

### `getEncryptionKey()` uses -1 to mean "no key"
`0xFFFFFFFF` is a legal key, so the sentinel is ambiguous in principle. In practice it is
unreachable: producing it requires a stream of 61,741 words — 246,964 pixels — which no DS
VRAM bank can hold. Recorded because the reasoning is not obvious, not because it needs
fixing.

The real defect at those lines is that `save()` passes the sentinel through as a live seed
without checking.

### Smaller items
- `Palette(int)` accepts any size; `Palette(Color[])` rejects more than 256. The first will
  happily emit a 1000-colour NCLR.
- The NCGR parse constructor has no positive-dimension guard, so a 48-byte file yields a
  zero-height image.
- `getSubImage` copies `scanMode` and `encryptionKey` onto a freshly laid-out image, where
  neither applies.
- Sizing the scanned decode buffer from the tile count means a truncated file now fails with
  "Not enough room to read" rather than a format error. It belongs in the malformed-input
  suite.

---

## PokEditor-Core

### The parsers have no coverage that runs in CI
Round-trip tests against real game data need a retail ROM and skip without one. Of the tests
that do run, most assert only that a parser is non-null. **A green build here says very
little**: no format's parse is exercised.

Run with `-Drom.dir=<dir>` before trusting a change to a parser. Expect failures — the
round-trip assertions were strengthened, and the original author's own code counted "valid
but non-1:1 matching" script files, which suggests scripts do not round-trip byte-exactly.

### `isEndCommand` treats `endstd` as terminating a run
An unannounced change to script traversal. If it is wrong, commands after an `endstd` are
never visited and are silently dropped on save. The macro file documents command 21 as
"Yield to parent context", so it is probably right — but it is the most consequential line
in the script reader and it is unverified against a real ROM.

### Abort-versus-tolerate is unresolved on the text and script load paths
`TextBankData` bounds checks and `offsetObtainer` now abort where they previously degraded.
For a retail ROM this is fine. For a hacked ROM — which is the population this tool
serves — it converts a file that opened with one garbled entry into a file that will not
open at all.

The same question was already settled the other way for evolution files, where a cap fixed
at the retail entry count made expanded tables unsavable. These two paths should be
consistent, and currently are not.

---

## Nds4j-ToolUI

### Only the file helper is tested; the module's actual job is not
**Severity: this is where the three items below live.**

3,834 lines of `src/main` against 588 of tests, and the tests reach two files: `FileUtils` and
the hexadecimal spinner's formatting. `Tool` (1,299 lines) has none, nor do `ToolFrame`,
`PanelManager`, `ProjectCreateDialog`, `ProjectStartPanel` or `ThemeUtils`.

That is the module owning project open and save, the save lock, and the git integration - and the
three defects below are all in `Tool`, `ToolFrame` and `PanelManager`, which is not a
coincidence. `FileUtils.atomicWrite` was the one part with tests, and it is the one part that got
fixed properly, because the tests could show what was wrong.

Worth stating precisely, because the raw numbers mislead in the other direction: the atomic-write
tests are genuine. Three of them turn on POSIX permissions and skip when the suite runs as root -
which is what a container does, so a local run reports `25 passed, 3 skipped`. Hosted runners are
not root, and CI reports `25 passed, 0 skipped`. Those three are the ones with teeth: they are
what proved the previous durability test asserted nothing.

### ~~The backup commit failed for anyone who signs commits with SSH~~ - fixed
`gpg.format = ssh` in a user's global config made JGit refuse the repository, with an unchecked
`IllegalArgumentException` that escaped the worker's handlers and reached the user as "an
unexpected error occurred" after **every save**. The save had worked; only the backup was missing,
and nothing distinguished the two.

Needed both a bump to JGit 7.1.0 and an explicit `setSign(false)` - on 6.7 and 6.10 the commit
throws whatever `setSign` says, and on 7.1 it throws unless signing is explicitly off. Signing an
automatic local backup was never meaningful anyway.

PokEditor also declared JGit itself, at 6.7.0, and a direct dependency beats a transitive one - so
upgrading ToolUI alone would have left the shipped application unchanged. That declaration is
removed rather than bumped: nothing in PokEditor references JGit, and pinning it again is how the
versions drift apart in the first place. **Let ToolUI own this version.**

The general lesson is worth more than the fix: no test had a repository configured the way a real
user's is. It surfaced only because this container happened to have that setting.

### ~~A multi-file save is not atomic~~ - fixed, with a named remainder
`FileUtils.atomicWrite` is split into staging and the rename it ends with, and `atomicWriteAll`
stages every entry before moving any of them. `Tool.SaveBatch` collects sections and writes them
as one; PokEditor's sheet save is now a single batch.

Staging is where a save actually fails - a full disk, a read-only file, a bad path - so those now
happen while every target is still the old version.

**What is not fixed**, and the javadoc says so rather than claiming otherwise: the run of renames
at the end is not one operation. Each is atomic alone, but a process killed between two of them
still leaves some files new and some old. Closing that needs a journal and a recovery pass on
open, which would be the next step if it is ever wanted.

### ~~Nine `writeModified*` methods document a boolean they cannot return~~ - fixed
They return `void` and throw. `writeProjectInfo` keeps its boolean, which has always meant
something. This is a source-breaking change for any caller testing the result, which is why it was
worth doing before 1.0.0 rather than after.

### ~~The git worker holds the save lock for the duration of a commit~~ - fixed
Held across staging only. The reason recorded here for holding it - that a partially written file
could be committed - was **wrong**: files are written through a temporary and renamed, so no file
is ever visible in a partial state. The real reason is that a save landing mid-staging commits
some files new and some old, and that reason justifies staging but not committing.

A save that still has to wait now waits a bounded time and says why, instead of freezing the
interface with nothing said. That is a mitigation, not a cure: saving still happens on the event
thread, and moving it off is the actual fix.

---

## Dependency declarations

Swept after the JGit pin turned out to be a class of problem rather than one mistake. The rule
that came out of it: **a module declares what it imports, and nothing else.** Both halves were
being broken.

*Declaring what is not used* - a direct declaration overrides what an upstream dependency asks
for, and since nothing imports it, a wrong version cannot fail to compile. JGit was this, and
shipped broken. Also removed: `jsvg` from ToolUI and PokEditor, which belongs to flatlaf-extras
and was pinned one version ahead of what flatlaf asks for; `flatlaf-intellij-themes` from ToolUI,
which never referenced it - `ThemeUtils` holds an empty list and the consumer supplies themes;
`jackson-dataformat-xml` from all three; `junit:junit` from Core and PokEditor, both entirely on
JUnit 5.

*Using what is not declared* - the same sweep proved why this matters. Removing
`jackson-dataformat-xml`, which nothing imported, also removed the only route to
`jackson-databind`, which several modules do import, and the build stopped compiling.
`jackson-databind` and `jackson-core` are declared explicitly now.

Guice moved to test scope in Core, where nothing in `src/main` imports it.

Two things worth keeping in mind for the next sweep:

- **`dependency:analyze` is a report, not a verdict.** It reads bytecode, so it called `jsvg` and
  `flatlaf-intellij-themes` unused in PokEditor, which genuinely uses both - through runtime
  loading it cannot see. Every entry above was checked against the source.
- **A dependency change proves nothing without `clean`.** The first pass here reported a green
  build on stale `target/classes`: the poms had changed but no source had, so nothing was
  recompiled. The clean build failed immediately.

### The remaining hazard, unfixed
`Nds4j` is declared at `1.0.0` in three poms, and `assertj` and `junit-jupiter` in all four.
Nothing disagrees today - every shared artifact was checked - but there is no parent pom or
`dependencyManagement`, so each is a place the versions can drift apart silently. That drift,
already happened, is what the JGit bug was.

---

## Merging and releasing

### Merge order
Nds4j merges **first**. All three others pin `Nds4j:1.0.0` and Nds4j `main` is still `0.1.0`;
their CI clones the sibling at a branch of the same name, or `main` when there is none, and
builds it from source. So once Nds4j's branch is on `main`, `main` is 1.0.0 and the other three
resolve without a matching branch. Until then they depend on their branches existing.

Publication to Central is **not** a merge prerequisite - an earlier version of this file said it
was, which was wrong: nothing in CI resolves these from Central. It is a prerequisite for users
and for the Nds4j README, which advertises a coordinate that 404s until the release is pushed by
hand from the portal.

### PokEditor's build is red and will stay red
Every run fails at *Verify dependencies resolve*, on the unpublished `VariableTracker` above, and
every step after it is skipped - including the jar build and its checks. This is the one thing
standing between PokEditor's PR and a green build; it is not a test failure and no amount of test
work will clear it.

### Publishing prerequisites, none of them code
- The signing key's subkey has expired. Extend it, then re-upload to **both**
  `keyserver.ubuntu.com` and `keys.openpgp.org` - it is currently on the first only.
- Four repository secrets must exist before `release.yml` can run: `MAVEN_CENTRAL_USERNAME`,
  `MAVEN_CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.
- ~~`Nds4j/.github/workflows/maven-publish.yml` publishes to the decommissioned OSSRH.~~
  **Deleted.** `release.yml` is now the only publish path.
- ~~`Nds4j/.github/workflows/maven-verify.yml` is the same vintage - JDK 8, Python 3.8.~~
  **Rewritten.** It fired on every push to `main`, so merging would have turned `main` red on
  JDK 8 for reasons unrelated to the merge. Now `workflow_dispatch` only, on build.yml's
  toolchain. It also changes the scanned-NCGR coverage note above.

### ~~Open decision blocking a permanent API~~ - settled
`CodeBinary.compressed` was private, had no getter, and was read nowhere - which is how it
carried an inverted value undetected. It is now public as `wasCompressed()`, and its tests ask
the object rather than reaching in by reflection.

Past tense because the flag describes the data the binary was **constructed from**, not what it
holds now: the buffer is decompressed either way and `getSize()` is the decompressed length, so a
retail arm9 answers `true` while everything read out of it is plain.

It went in as `isCompressed()` first, and that was wrong twice over. `Overlay` extends
`CodeBinary` and already has an `isCompressed()` - the compression bit the ROM's overlay table
stores, a different fact and a settable one - so the new method was silently overridden. Same
signature, no `@Override`, nothing from the compiler, and a `CodeBinary` reference to an
`Overlay` quietly answering the other question. The two can disagree, and a test now pins that
they do. Worth remembering as the shape of the risk: adding a name to a base class can capture a
subclass's existing method without a word from the compiler.
