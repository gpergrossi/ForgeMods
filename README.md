# AeroGen for [Fabric](https://fabricmc.net/) 0.7.4 (Minecraft 1.16)

Generates a world full of beautiful floating islands.

![Development Screenshot](images/ktaqncffgzg01.png)

This build is compatable with Minecraft 1.16 running Fabric 0.7.4+

# Images Directory

There are a number of images documenting development work and inspiration available [here](https://drive.google.com/drive/folders/1Gf60RvpDF6PWzwxIvtzchbF7EmpCd4j-?usp=sharing).

# Trello

Follow the mod's development on [Trello](https://trello.com/b/sjkYZNq2/aerogen-development)

# Install Java 16

Minecraft is now using Java 16. You will need a Java 16 JDK to develop mods.
1. Install a JDK for Java 16. One option is [here](https://www.oracle.com/java/technologies/javase-jdk16-downloads.html) or you can use [AdoptOpenJDK](https://marketplace.eclipse.org/content/java-16-support-eclipse-2021-03-419#group-details).
2. Make sure your JAVA_HOME system variable is set to the new Java 16 JDK.

# Eclipse Setup

## Install Latest Eclipse (for Java 16 support)
1. Install the latest [Eclipse IDE](https://www.eclipse.org/downloads/)
2. For now, you need a [plugin](https://marketplace.eclipse.org/content/java-16-support-eclipse-2021-03-419#group-details) to support Java 16 in Eclipse because it's that new. 
3. You probably also want a Gradle extension like [Buildship Gradle](https://marketplace.eclipse.org/content/buildship-gradle-integration?mpc=true&mpc_state=) from the Eclipse foundation.
4. If eclipse bugs you to install plugins for new file types (like .json) that's up to you, but I just point them to my System Editor (Notepad++).

## Project Import
1. Clone this github repository
2. Use `git checkout fabric/development` to checkout this fabric development branch.
3. Before you can import the project into eclipse you have to execute some Gradle tasks to generate the project files.
   1. Open a command prompt or whatever console you use
   2. Navigate to <aerogen repository>/aerogen/
   3. Run `gradlew eclipse` (`gradlew tasks` will list other tasks you can use)
   4. Run `gradlew genEclipseRuns` to generate the client and server run configurations for eclipse
4. Make a new workspace specifically for AeroGen. (Optional, but recommended)
5. In eclipse: 
   1. Import existing project...
   2. Locate your local aerogen reposiotory directory
   3. It should identify the aerogen project
   4. Click finish
   5. Ignore console errors, but you may want to check your Run Configurations and make sure you can execute the aerogen_client build.

# Mod Development Info

## Attaching Sources
   
   If you need to attach sources and you IDE is not making it easy. Run `gradlew genSources` (takes a while) then locate the sources in `C:\Users\<username>\.gradle\caches\fabric-loom\1.17-mapped-net.fabricmc.yarn-1.17+build.11-v2\minecraft-1.17-mapped-net.fabricmc.yarn-1.17+build.11-v2-sources.jar`. Obviously the version info changes depending on all of your various versions (minecraft, fabric, yarn, etc.) but it was easier to show a full example.
