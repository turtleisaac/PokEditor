# PokEditor v3

**Author: Turtleisaac**

Multifunctional in-depth editor for Pokémon Gen IV (4) game data.

Written entirely in Java and is completely OS-agnostic.

**Java 20 is required.** Newer versions may work, but if older classes have been deprecated or removed in those versions, then it may not work for those higher versions of Java.
* The majority of the backend can be found in my other library, [PokEditor-Core](https://github.com/turtleisaac/PokEditor-Core).
* Also powered by my other libraries, [Nds4j](https://github.com/turtleisaac/Nds4j) and [Nds4j-ToolUI](https://github.com/turtleisaac/Nds4j-ToolUI).
* This tool is still in active development and will receive periodic updates as improvements are made and bugs are found, time permitting.
* Please join [this Discord server](https://discord.gg/zAtqJDW2jC) for help with using PokEditor v3 or for help with any questions relating to Pokémon Gen 4 and 5 hacking.
* Currently can be used in English and has partial French and Chinese support.

<img width="403" alt="image" src="https://github.com/turtleisaac/PokEditor/assets/7987859/8165bcd7-e9af-4056-991a-d61cc5783fe7">

<img width="1312" alt="image" src="https://github.com/turtleisaac/PokEditor/assets/7987859/1608d39f-3a29-4bbd-ab53-ea50d87ffff1">

<img width="1044" alt="image" src="https://github.com/turtleisaac/PokEditor/assets/7987859/7afc9ed8-0a53-472e-9aae-e1bee7d66fe3">


# Usage

Builds are available in the [Releases page here on GitHub](https://github.com/turtleisaac/PokEditor/releases). Simply download the most recent release, unzip the zip file, and double click the file contained within with the name **PokEditor.jar**!

Unlike prior versions of PokEditor, v3 is intended to fully be used within the tool. No more exporting sheets or editing them elsewhere. Additionally, there is very little effort required by the user to get it set up this time around. No more annoying sheets setup process, it should just automatically load everything into the sheets the instant you open a project.

# Building from source

Requires a JDK (the module compiles at source/target 20; CI builds on 21) and Maven.

### 1. Install the sibling libraries first, in this order

None of the three are on Maven Central at the versions this pom pins, so each has to be built
and installed locally before PokEditor will resolve. Order matters: Nds4j underpins the other
two.

```
git clone https://github.com/turtleisaac/Nds4j.git          && mvn -f Nds4j/pom.xml          install -DskipTests
git clone https://github.com/turtleisaac/Nds4j-ToolUI.git   && mvn -f Nds4j-ToolUI/pom.xml   install -DskipTests
git clone https://github.com/turtleisaac/PokEditor-Core.git && mvn -f PokEditor-Core/pom.xml install -DskipTests
```

When a change spans several of these repositories it is developed on branches of the same name
in each, so check out matching branches before installing. CI does this automatically, falling
back to `main` where no matching branch exists.

> **A clean checkout does not build yet.** `pom.xml` also declares
> `io.github.turtleisaac:VariableTracker:1.0-SNAPSHOT`, which is published nowhere the build can
> reach and has no `<repositories>` entry. The field script editor is disabled, but
> `ScriptDocument` still imports the library, so it is still needed to compile. See
> `TECH_DEBT.md`.

### 2. Build the runnable jar

```
mvn clean -Pdist package
```

This produces **`target/PokEditor-3.2.0-dist.jar`**, which is the artifact to hand to a tester or
attach to a release — every dependency in one archive, launched with `java -jar`. Add
`-DskipTests` to skip the suite.

The plain `mvn package` deliberately does **not** produce it: `target/PokEditor-3.2.0.jar` holds
this module's classes without its dependencies and is not runnable on its own. `dist` is a
separate profile because it costs ~17MB of output that nothing in CI consumes. (Under `-Pdist`
that plain jar also picks up the unpacked `WinLaF` classes as a side effect of the step below.
Nothing distributes it, so this is harmless — but it is why a `-Pdist` build should start from
`clean`.)

### Do not hand-assemble the jar

The `dist` profile does two things that are easy to miss, and getting either wrong produces a
jar that starts fine and then dies the first time someone types into a combo box in a sheet:

- **It writes an `Add-Exports` manifest entry.** jide-oss predates the module system and does
  `instanceof com.sun.java.swing.plaf.windows.WindowsLookAndFeel` on a path every JIDE component
  reaches. On Windows that package lives in `java.desktop`, which does not export it, so the
  check fails with `IllegalAccessError` before it can answer false.
- **It unpacks `WinLaF.jar`.** That dependency is `<scope>system</scope>`, and the shade plugin
  resolves only compile and runtime scope, so it is dropped silently. On Linux and macOS no JDK
  ships that package at all and this copy is the only thing satisfying it, so without it the
  same code path dies with `NoClassDefFoundError` instead.

Neither substitutes for the other — on Windows, parent-first delegation finds `java.desktop`'s
copy and shadows `WinLaF.jar` entirely. `TECH_DEBT.md` has the full account.

### CI builds this too

Every push builds the same jar and uploads it as a run artifact named **PokEditor-jar**, so a
tester can download it from the Actions run rather than waiting for a release. CI also verifies
it: it runs the look-and-feel resolution described above against the built jar, once under this
runner's own conditions and once under a simulated Windows one, and then checks that the second
fails when the manifest entry is withheld — because a check that passes either way would go green
on exactly the artifact that crashes.

### 3. Run it

```
java -jar target/PokEditor-3.2.0-dist.jar
```

### Running the tests

```
mvn verify -Djava.awt.headless=true -DexcludedGroups=dead-code
```

This is what CI's `Build` step runs, and it must stay green. The `dead-code` tag marks tests for
classes with no callers anywhere in `src/main` — they are the specification for whoever revives
or deletes that code, not defects anyone can hit. They are expected to fail, so omitting
`-DexcludedGroups` will report failures on a healthy tree.

Those tests run in CI's separate `Known dead-code failures` step, which pins both the tagged
count and the failure count. Pinning them means a regression cannot be hidden by tagging it, and
a fix prompts someone to remove the tag — so if you add or fix a tagged test, update
`EXPECTED_TAGGED` / `EXPECTED_RED` in `.github/workflows/build.yml` and say why in the commit.

# List of Spreadsheet-Based Editors

* Personal Data Editor

* TM Learnset Editor)

* Level-Up Learnset Editor

[//]: # ()
[//]: # (* Encounter Editor &#40;also has a GUI-based editor&#41; &#40;currently incomplete&#41;)

* Evolutions Editor

[//]: # ()
[//]: # (* Item Editor)

* Move Editor

[//]: # ()
[//]: # (* Move Tutor Editor &#40;moves taught and compatibility&#41;)

[//]: # ()
[//]: # (* Baby Form Editor &#40;what hatches from an egg&#41;)

[//]: # ()
[//]: # (* Trainer Editor &#40;also has a GUI-based editor&#41;)

# GUI-Based Editors

[//]: # ()
[//]: # (* Trainer Editor)

[//]: # (  * Trainer Text Editor)

[//]: # (  * Nature & IV Calculator)

[//]: # (  * Smogon Format Team Import/Export)

* Pokémon Sprite Editor

[//]: # (  * Palette Editor)

[//]: # (  * Sprite XY-Coordinate Placement Editor)

[//]: # (  * Sprite Shadow Placement Editor)

[//]: # (  * Sprite Shadow Size Editor)

[//]: # (  * Send-out Movement/Animation Editor)
