# DreamBot Scripting: The Master Guide

Welcome to the ultimate guide for building a Tutorial Island script in DreamBot! This comprehensive resource takes you from beginner basics to professional-level scripting techniques. Whether you're new to Java or just new to bot development, we'll break down each concept with clear explanations, simple examples, and practical tips.

By the end of this guide, you'll have all the knowledge needed to create a fully functional Tutorial Island script that can handle NPCs, inventory, navigation, dialogues, and more—all while staying undetected with smart anti-ban measures.

---

## 📑 Table of Contents
0. [Development Environment Setup](#0-development-environment-setup)
   - [Setting up IntelliJ IDEA](#setting-up-intellij-idea)
   - [Alternative: VS Code Setup](#alternative-vs-code-setup)
   - [Setting up DreamBot Repository](#setting-up-dreambot-repository)
   - [Compiling Java to JAR with Java 11](#compiling-java-to-jar-with-java-11)
   - [Testing Your Setup](#testing-your-setup)
1. [Core Setup & Imports](#1-core-setup--imports)
   - [Basic Script Structure](#basic-script-structure)
   - [ScriptManifest Parameters](#scriptmanifest-parameters)
   - [The onLoop Method](#the-onloop-method)
   - [Essential Imports Explained](#essential-imports-explained)
2. [Developer Tools (Data Gathering)](#2-developer-tools-data-gathering)
   - [Developer Mode (CTRL+D)](#developer-mode-ctrl--d)
   - [Game Explorer (CTRL+G)](#game-explorer-ctrl--g)
   - [Console (CTRL+L)](#console-ctrl--l)
3. [Game Object Interactions](#25-game-object-interactions)
   - [Finding and Interacting with Game Objects](#finding-and-interacting-with-game-objects)
   - [Using Filters for Specific Objects](#using-filters-for-specific-objects)
   - [NPC Interactions](#npc-interactions)
   - [Ground Item Pickup](#ground-item-pickup)
   - [Fishing and Similar Spot Interactions](#fishing-and-similar-spot-interactions)
4. [World Navigation (Walking)](#3-world-navigation-walking)
   - [Basic Walking Pattern](#basic-walking-pattern)
   - [Search, Walk, Interact Pattern](#search-walk-interact-pattern)
   - [Defining Areas with Coordinates](#defining-areas-with-coordinates)
   - [Enabling Run Energy](#enabling-run-energy)
   - [Walking to a Distant Area (Not On Screen)](#walking-to-a-distant-area-not-on-screen)
   - [Door Interaction Example](#door-interaction-example)
   - [World Hopping](#world-hopping)
   - [Grand Exchange Trading](#grand-exchange-trading)
   - [Widget Interactions](#widget-interactions)
   - [PlayerSettings (Varps and Varbits)](#playersettings-varps-and-varbits)
5. [Inventory Management](#4-inventory-management)
   - [Basic Inventory Checks](#basic-inventory-checks)
   - [Item Interactions](#item-interactions)
   - [Using an Inventory Item on a Game Object](#using-an-inventory-item-on-a-game-object)
   - [Dropping Items](#dropping-items)
   - [Banking Operations](#banking-operations)
6. [Dialogue Handling](#5-dialogue-handling)
   - [Basic Dialogue Check](#basic-dialogue-check)
   - [Continuing Dialogues](#continuing-dialogues)
   - [Choosing Options](#choosing-options)
7. [Progress Tracking (Varp 281)](#6-progress-tracking-varp-281)
   - [Reading Progress](#reading-progress)
   - [Switch Statement for Progress](#switch-statement-for-progress)
8. [Mouse & Anti-Ban Philosophy](#7-mouse--anti-ban-philosophy)
   - [Human-like Delays](#human-like-delays)
   - [Gaussian Distribution](#gaussian-distribution)
   - [Micro-Actions](#micro-actions)
   - [Vary Your Patterns](#vary-your-patterns)
9. [Data Recording Template](#8-data-recording-template)
10. [Putting It All Together: A Complete Example](#9-putting-it-all-together-a-complete-example)
11. [Troubleshooting Common Issues](#10-troubleshooting-common-issues)
    - [GUI Performance Issues](#gui-performance-issues)
    - [Script Not Loading](#script-not-loading)
    - [Compilation Errors](#compilation-errors)
    - [Runtime Errors](#runtime-errors)

---

## 0. Development Environment Setup
Before you start coding, you need to set up your development environment. This section covers installing VS Code, Java 11, and the DreamBot client.

### Setting up IntelliJ IDEA
IntelliJ IDEA Community Edition is the recommended IDE for DreamBot scripting (as per official DreamBot documentation).

1. **Download and Install IntelliJ IDEA**
   - Go to [jetbrains.com/idea/download](https://www.jetbrains.com/idea/download)
   - Download the Community Edition (free)
   - Install following the setup wizard

2. **Create a New Project**
   - Open IntelliJ IDEA
   - Click "New Project"
   - Select "Java" as the project type
   - Name your project (e.g., "my-scripts")
   - Choose IntelliJ as the build system
   - Select your Java 11 JDK
   - Uncheck "Add sample code"
   - Expand "Advanced Settings" and set Module name (e.g., "tutorial-script")

3. **Configure Project Language Level**
   - Go to File > Project Structure > Project
   - Set Language level to "11 - Local variable syntax for lambda parameters"

4. **Add DreamBot Libraries**
   - In Project Structure > Libraries tab
   - Click "+" > Java
   - Navigate to: `C:\Users\YourName\DreamBot\BotData\repository2\` (Windows)
     or `/home/YourName/DreamBot/BotData/repository2/` (Linux/Mac)
   - Select the folder and add it to your module

5. **Set Up JAR Artifact**
   - In Project Structure > Artifacts tab
   - Click "+" > JAR > Empty
   - Name it (e.g., "tutorial-script")
   - Set Output directory to DreamBot Scripts folder
   - Add your module's compile output to the artifact

### Alternative: VS Code Setup
If you prefer VS Code over IntelliJ:

1. **Install VS Code**
   - Go to [code.visualstudio.com](https://code.visualstudio.com/)
   - Download and install VS Code

2. **Install Java Extension Pack**
   - Open VS Code
   - Go to Extensions (Ctrl+Shift+X)
   - Search for "Extension Pack for Java" by Microsoft
   - Click Install

3. **Configure Java 11 Runtime**
   - In VS Code, open Command Palette (Ctrl+Shift+P)
   - Type "Java: Configure Java Runtime"
   - Select Java 11 as your runtime

4. **Create a VS Code Project**
   - Create a new folder for your project (e.g., "my-dreambot-scripts")
   - Open the folder in VS Code
   - Create a `src` folder for your Java files
   - Create a `.vscode` folder for VS Code configuration

5. **Add DreamBot Libraries to VS Code**
   - Create a `.classpath` file in your project root:
   
     **What is the project root?** The project root is the top-level folder of your project (e.g., "my-dreambot-scripts"). This is the folder you opened in VS Code and contains your `src` folder and `.vscode` directory.
   
     ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <classpath>
         <classpathentry kind="src" path="src"/>
         <classpathentry kind="lib" path="C:/Users/YourName/DreamBot/BotData/repository2"/>
         <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-11"/>
         <classpathentry kind="output" path="bin"/>
     </classpath>
     ```
     *Replace `YourName` with your actual Windows username*

   - **Alternative method using workspace settings:**
     - Create `.vscode/settings.json` in your project:
       ```json
       {
         "java.project.referencedLibraries": [
           "C:/Users/YourName/DreamBot/BotData/repository2/**/*.jar"
         ],
         "java.configuration.runtimes": [
           {
             "name": "JavaSE-11",
             "path": "C:/path/to/your/jdk-11",
             "default": true
           }
         ]
       }
       ```

6. **Set Java Source Level to 11**
   - In `.vscode/settings.json`, add:
     ```json
     {
       "java.compile.source": "11",
       "java.compile.target": "11"
     }
     ```

7. **Create VS Code Build Task**
   - Create `.vscode/tasks.json`:
     ```json
     {
       "version": "2.0.0",
       "tasks": [
         {
           "label": "build",
           "type": "shell",
           "command": "javac",
           "args": [
             "-cp",
             "C:/Users/YourName/DreamBot/BotData/repository2/*",
             "-d",
             "bin",
             "src/*.java"
           ],
           "group": "build",
           "presentation": {
             "echo": true,
             "reveal": "silent",
             "focus": false,
             "panel": "shared"
           }
         }
       ]
     }
     ```

**VS Code Project Structure:**
```
my-dreambot-scripts/
├── .vscode/
│   ├── settings.json
│   └── tasks.json
├── .classpath (optional)
├── src/
│   └── Main.java
└── bin/ (created by build)
```

### Setting up DreamBot Repository
DreamBot provides the API and client you'll need for your scripts.

1. **Download DreamBot Client**
   - Go to [dreambot.org](https://dreambot.org/)
   - Download the latest client
   - Run the installer

2. **Locate the Scripts Folder**
   - After installation, find your DreamBot directory
   - Usually: `C:\Users\YourName\DreamBot\Scripts\` on Windows
   - This is where you'll place your compiled JAR files

3. **Get the DreamBot API**
   - The DreamBot client includes the API libraries
   - Usually located in `BotData/repository2/` folder
   - You'll need this for compilation and IDE setup

### Compiling Java to JAR with Java 11
Once your code is ready, compile it into a JAR file that DreamBot can load.

#### Method 1: Command Line Compilation
1. **Prepare your files**
   - Create a `src` folder for your Java files
   - Place your script (e.g., `TutorialScript.java`) in `src/`

2. **Compile with javac**
   ```bash
   # Navigate to your project directory
   cd C:\path\to\your\project

   # Compile (replace paths with your actual DreamBot location)
   javac -cp "C:\path\to\DreamBot\BotData\repository2\*" -d build src\*.java
   ```

3. **Create JAR file**
   ```bash
   # Create manifest file
   echo "Main-Class: TutorialScript" > manifest.txt

   # Create JAR
   jar cfm TutorialScript.jar manifest.txt -C build .
   ```

4. **Move to DreamBot Scripts folder**
   ```bash
   # Copy to DreamBot scripts directory
   copy TutorialScript.jar "C:\Users\YourName\DreamBot\Scripts\"
   ```

#### Method 2: Using a Build Script (Recommended)
Create a `build.bat` file in your project root:

```batch
@echo off
REM Build script for DreamBot scripts

REM Set your paths here
set DREAMBOT_LIB="C:\path\to\DreamBot\BotData\repository2"
set SRC_DIR=src
set BUILD_DIR=build
set JAR_NAME=TutorialScript.jar
set MAIN_CLASS=TutorialScript

REM Create build directory
if not exist %BUILD_DIR% mkdir %BUILD_DIR%

REM Compile
javac -cp "%DREAMBOT_LIB%\*" -d %BUILD_DIR% %SRC_DIR%\*.java

REM Create manifest
echo Main-Class: %MAIN_CLASS% > manifest.txt

REM Create JAR
jar cfm %JAR_NAME% manifest.txt -C %BUILD_DIR% .

REM Clean up
del manifest.txt
rd /s /q %BUILD_DIR%

echo Build complete! Copy %JAR_NAME% to your DreamBot Scripts folder.
pause
```

#### Method 3: Using VS Code Tasks
Create a `.vscode/tasks.json` file in your project:

```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "build",
            "type": "shell",
            "command": "javac",
            "args": [
                "-cp",
                "C:\\\\path\\\\to\\\\DreamBot\\\\BotData\\\\repository2\\\\*",
                "-d",
                "build",
                "src\\*.java"
            ],
            "group": "build",
            "presentation": {
                "echo": true,
                "reveal": "silent",
                "focus": false,
                "panel": "shared"
            }
        }
    ]
}
```

### Testing Your Setup
1. **Open DreamBot Client**
   - Launch DreamBot
   - Go to Script Manager
   - Your JAR should appear in the list

2. **Run Your Script**
   - Select your script
   - Click Start
   - Check the console for any errors

3. **Debug in VS Code**
   - Use the integrated terminal in VS Code
   - Run your build script
   - Copy the JAR to DreamBot Scripts folder

**Troubleshooting Tips:**
- Make sure Java 11 is set as the active runtime in VS Code
- Verify the DreamBot BotData/repository2/ path is correct
- Check that your main class name matches exactly
- Ensure all imports are available in the DreamBot API

---

## 1. Core Setup & Imports
Every DreamBot script begins with a solid foundation. Your script must extend `AbstractScript`, which provides the framework for the bot's lifecycle. The `@ScriptManifest` annotation tells DreamBot about your script's details.

### Basic Script Structure
```java
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;

@ScriptManifest(
    name = "Tutorial Island Script",
    author = "YourName",
    description = "Completes Tutorial Island automatically",
    category = Category.MISC,
    version = 1.0
)
public class TutorialScript extends AbstractScript {

    @Override
    public void onStart() {
        // Initialize your script here
        Logger.log("Script started!");
    }

    @Override
    public int onLoop() {
        // Main script logic goes here
        return 1000; // Wait 1 second before next loop
    }

    @Override
    public void onExit() {
        // Cleanup when script stops
        Logger.log("Script stopped!");
    }
}
```

### ScriptManifest Parameters
The `@ScriptManifest` annotation requires several parameters to properly register your script with DreamBot:

- `name`: The script name shown in the Script Manager (required)
- `author`: Your name or developer name (required)  
- `description`: Brief description of what the script does (optional)
- `category`: Script category from the `Category` enum (required)
- `version`: Version number as a double (required)

**Example with all parameters:**
```java
@ScriptManifest(
    name = "My Tutorial Script",
    author = "DreamBotUser",
    description = "Completes Tutorial Island with anti-ban features",
    category = Category.MISC,
    version = 1.0
)
```

### The onLoop Method
The `onLoop()` method is the heart of your script. It's called repeatedly by DreamBot until the script stops. The return value tells DreamBot how many milliseconds to wait before calling `onLoop()` again.

**Simple Example**: Basic logging script
```java
@Override
public int onLoop() {
    Logger.log("Script is running!");
    return 1000; // Run every 1 second
}
```

### Essential Imports Explained
Here's what each import does and why you need it:

#### Core Script Structure
- `AbstractScript`: The base class for all scripts
- `ScriptManifest`: Defines script metadata (name, author, version, category)
- `Category`: Script categories for the Script Manager

#### Game Object & NPC Interaction
- `NPCs`: Interact with non-player characters (talk, attack, check status)
- `GameObjects`: Handle objects in the game world (trees, doors, furnaces, etc.)
- `GroundItems`: Pick up dropped items from the ground
- `Widgets`: Interact with UI elements and interface buttons

#### Player Management
- `Inventory`: Manage your player's inventory (check, drop, use items)
- `Players`: Access player information and local player data
- `PlayerSettings`: Check Varps and Varbits for game state
- `Bank`: Interact with banks (open, deposit, withdraw)
- `Skills`: Check and manage skill levels and experience

#### Navigation & Movement
- `Walking`: Navigate the game world and use pathfinding
- `Area`: Define rectangular areas for location checking
- `Tile`: Specify exact coordinates for movement and calculations
- `WorldHopper`: Change worlds and hop to different servers

#### Trading & Economics
- `GrandExchange`: Buy and sell items on the Grand Exchange
- `Shops`: Interact with NPC shops for buying/selling

#### User Interaction & Timing
- `Dialogues`: Handle NPC conversations and dialogue options
- `Sleep`: Add delays and pauses between actions
- `Mouse`: Control mouse movement and clicking
- `Camera`: Rotate and adjust the game camera

#### Utilities & Helpers
- `Logger`: Print debug messages and logging information
- `Random` (Java): Generate random numbers for variability
- `Calculations`: Mathematical utilities (random numbers, distance, angles)

#### Common Import Statements
```java
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.grounditems.GroundItems;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Bank;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.Area;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.WorldHopper;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.wrappers.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Mouse;
import org.dreambot.api.utilities.Camera;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
```

**Simple Example**: Basic script with essential imports
```java
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Logger;
import java.util.Random;

@ScriptManifest(
    name = "My Tutorial Script",
    author = "YourName",
    category = Category.MISC,
    version = 1.0
)
public class MyScript extends AbstractScript {
    // Your script code here
}
```

---

## 2. Developer Tools (Data Gathering)
DreamBot provides powerful tools to inspect the game world, just like a bot would "see" it. These tools are crucial for gathering the IDs and data you'll need for your script.

### Developer Mode (CTRL + D)
- **What it does**: Shows names and IDs of everything you hover over
- **How to use**:
  1. Press `CTRL + D` to toggle developer mode
  2. Hover over NPCs, objects, or items
  3. Note the ID numbers that appear
- **Example**: Hovering over the Gielinor Guide shows "NPC: Gielinor Guide (ID: 3308)"

### Game Explorer (CTRL + G)
- **What it does**: Deep dive into game data
- **Key tabs**:
  - **Player Settings**: Find Varp values (like Varp 281 for tutorial progress)
  - **Widgets**: See interface components and their IDs
  - **Inventory**: Check item IDs
- **How to use**: Open with `CTRL + G`, navigate tabs, record important values

### Console (CTRL + L)
- **What it does**: Debug your script in real-time
- **How to use**: Use `Logger.log("Your message here");` in your code
- **Example**:
```java
Logger.log("Current progress: " + PlayerSettings.getConfig(281));
```

**Pro Tip**: Always test your script with these tools open to verify you're interacting with the correct game elements.

---

## 2.5 Game Object Interactions
Interacting with objects in the game world like trees, doors, and furnaces.

### Finding and Interacting with Game Objects
```java
// Find closest object by name
GameObject tree = GameObjects.closest("Tree");
if (tree != null && tree.interact("Chop down")) {
    Sleep.sleepUntil(() -> !Players.localPlayer().isAnimating(), 5000);
}

// Find object by ID
GameObject oakTree = GameObjects.closest(1281); // Oak tree ID
if (oakTree != null && oakTree.interact("Chop down")) {
    Sleep.sleepUntil(() -> Inventory.contains("Oak logs"), 10000);
}
```

### Using Filters for Specific Objects
```java
// Find objects with custom filters
GameObject bankChest = GameObjects.closest(obj -> 
    obj != null && 
    obj.getName().equals("Bank chest") && 
    obj.distance() <= 10
);
if (bankChest != null && bankChest.interact("Use")) {
    Sleep.sleepUntil(Bank::isOpen, 5000);
}
```

### NPC Interactions
```java
// Find and attack NPCs
NPC goblin = NPCs.closest("Goblin");
if (goblin != null && !goblin.isInCombat()) {
    if (goblin.interact("Attack")) {
        Sleep.sleepUntil(() -> Players.localPlayer().isInCombat(), 5000);
    }
}

// Find NPC by ID
NPC man = NPCs.closest(3106); // Man NPC ID
if (man != null && man.interact("Talk-to")) {
    Sleep.sleepUntil(Dialogues::inDialogue, 5000);
}
```

### Ground Item Pickup
```java
// Pick up items by name
GroundItem coin = GroundItems.closest("Coins");
if (coin != null && coin.interact("Take")) {
    Sleep.sleepUntil(() -> !coin.exists(), 3000);
}

// Pick up items by ID
GroundItem bones = GroundItems.closest(526); // Bones ID
if (bones != null && bones.interact("Take")) {
    Sleep.sleepUntil(() -> Inventory.contains("Bones"), 3000);
}

// Pick up from a list of items
List<GroundItem> items = GroundItems.all(item -> 
    item != null && 
    (item.getName().equals("Coins") || item.getName().equals("Bronze sword"))
);
for (GroundItem item : items) {
    if (item.interact("Take")) {
        Sleep.sleep(1000);
    }
}
```

### Fishing and Similar Spot Interactions
```java
// Find and fish at a fishing spot
GameObject fishingSpot = GameObjects.closest("Fishing spot");
if (fishingSpot != null) {
    if (!Players.localPlayer().isAnimating()) {
        if (fishingSpot.interact("Net")) {
            Sleep.sleepUntil(() -> Players.localPlayer().isAnimating(), 5000);
        }
    }
    // Optional: Wait until inventory is full or action is done
    Sleep.sleepUntil(() -> Inventory.isFull() || !Players.localPlayer().isAnimating(), 10000);
}

// Alternative: Use specific action like 'Lure' or 'Cage'
GameObject flyFishingSpot = GameObjects.closest("Fishing spot");
if (flyFishingSpot != null && flyFishingSpot.hasAction("Lure")) {
    flyFishingSpot.interact("Lure");
    Sleep.sleepUntil(() -> Players.localPlayer().isAnimating(), 5000);
}

// Interacting with similar game object actions (e.g., hopping in/out, mining, cutting)
GameObject oakTree = GameObjects.closest("Oak");
if (oakTree != null && oakTree.interact("Chop down")) {
    Sleep.sleepUntil(() -> Players.localPlayer().isAnimating(), 5000);
}

GameObject rock = GameObjects.closest("Rocks");
if (rock != null && rock.interact("Mine")) {
    Sleep.sleepUntil(() -> Players.localPlayer().isAnimating(), 5000);
}
```

---

## 3. World Navigation (Walking)
Moving around RuneScape efficiently is key to a smooth script. Avoid random clicking—use smart pathfinding instead.

### Basic Walking Pattern
```java
// Check if we can walk, then walk to a specific tile
if (Walking.shouldWalk()) {
    Walking.walk(new Tile(3094, 3107)); // Walk to Gielinor Guide
}
```

### Search, Walk, Interact Pattern
This is the most common navigation method:
```java
NPC guide = NPCs.closest("Gielinor Guide");
if (guide != null) {
    if (guide.distance() > 5) {
        if (Walking.shouldWalk()) {
            Walking.walk(guide);
        }
    } else {
        guide.interact("Talk-to");
        Sleep.sleepUntil(() -> Dialogues.inDialogue(), 5000);
    }
}
```

### Defining Areas with Coordinates
Areas are rectangular regions in the game world defined by coordinate boundaries. Use DreamBot's Game Explorer (CTRL+G) to find coordinates by hovering over tiles.

#### Rectangular Area (x1, y1, x2, y2)
```java
// Define a rectangular area: Area(x1, y1, x2, y2, plane)
// x1,y1 = bottom-left corner, x2,y2 = top-right corner
Area lumbridgeCastle = new Area(3205, 3209, 3215, 3223, 0);

// Check if player is in the area
if (!lumbridgeCastle.contains(Players.localPlayer())) {
    if (Walking.shouldWalk()) {
        Walking.walk(lumbridgeCastle.getRandomTile());
    }
}
```

#### How to Find Coordinates
1. **Open Game Explorer**: Press `CTRL + G` in DreamBot client
2. **Navigate to Player tab**: Shows current player coordinates
3. **Hover over tiles**: Coordinates appear in the bottom-left
4. **Record boundaries**: Walk to corners of your desired area and note the coordinates

#### Circular Area (Center + Radius)
```java
// Define a circular area: Area(centerX, centerY, radius, plane)
Area fishingSpot = new Area(3100, 3100, 5, 0); // 5-tile radius around fishing spot

// Use for proximity checks
if (fishingSpot.contains(Players.localPlayer())) {
    // Player is near the fishing spot
}
```

#### Area Methods
```java
Area myArea = new Area(3200, 3200, 3210, 3210, 0);

// Check if a tile is inside the area
Tile targetTile = new Tile(3205, 3205);
if (myArea.contains(targetTile)) {
    // Tile is within the area
}

// Get a random tile within the area
Tile randomTile = myArea.getRandomTile();
Walking.walk(randomTile);

// Get the center tile of the area
Tile centerTile = myArea.getCenter();
```

#### Common Area Patterns
```java
// Bank area for quick access
Area bankArea = new Area(3091, 3488, 3098, 3499, 0);

// Safe spot for combat training
Area safeSpot = new Area(3100, 3100, 3105, 3105, 0);
```

### Enabling Run Energy
Always enable run before walking to save time. Check with `Walking.isRunEnabled()` and use `Walking.toggleRun()` to turn it on.

```java
// Enable run if it's off and we have energy
if (!Walking.isRunEnabled() && Walking.getRunEnergy() > 10) {
    Walking.toggleRun();
    Sleep.sleep(300);
}
```

A good place to call this is at the top of `onLoop()` so run stays on throughout the script.

### Walking to a Distant Area (Not On Screen)
For tiles that are far away and off-screen, use `Walking.walkPath()` with a `TilePath` or simply call `Walking.walk()` — DreamBot's web walker will automatically pathfind across the map. You do **not** need the tile to be on screen.

```java
// Walk to a distant tile — web walker handles pathfinding automatically
Tile destination = new Tile(3105, 3095, 0);
if (!destination.equals(Players.localPlayer().getTile())) {
    Walking.walk(destination);
    // Wait until close enough before doing anything else
    Sleep.sleepUntil(() -> destination.distance() < 5, 15000);
}
```

For walking to an `Area`, use `getRandomTile()` the same way:
```java
Area targetArea = new Area(3105, 3093, 3100, 3098);
if (!targetArea.contains(Players.localPlayer())) {
    Walking.walk(targetArea.getRandomTile());
    Sleep.sleepUntil(() -> targetArea.contains(Players.localPlayer()), 15000);
}
```

**Key points:**
- `Walking.walk()` uses the web walker — no manual waypoints needed for distant tiles
- Always pair with `Sleep.sleepUntil()` so the next action waits until the player arrives
- Increase the timeout (third arg) for longer distances — 15000ms is safe for most Tutorial Island walks
- Enable run first (see above) so the player doesn't walk the whole way

### Door Interaction Example
Doors are interactive `GameObject`s with actions like "Open" or "Pass-through".

#### Open a single door by name
```java
GameObject door = GameObjects.closest("Door");
if (door != null && door.exists() && door.hasAction("Open")) {
    if (door.interact("Open")) {
        Sleep.sleepUntil(() -> !door.exists() || !door.isOnScreen(), 5000);
    }
}
```

#### Open a door by ID
```java
int doorId = 14803; // Example door ID; adjust to your door
GameObject door = GameObjects.closest(doorId);
if (door != null) {
    if (door.interact("Open")) {
        Sleep.sleepUntil(() -> !door.exists() || !door.isOnScreen(), 5000);
    }
}
```

#### Walk to a door and open it
```java
GameObject door = GameObjects.closest("Door");
if (door != null) {
    if (door.distance() > 3) {
        if (Walking.shouldWalk()) {
            Walking.walk(door);
            Sleep.sleepUntil(() -> door.distance() <= 3, 5000);
        }
    }
    if (door.isOnScreen() && door.interact("Open")) {
        Sleep.sleepUntil(() -> !door.exists() || Players.localPlayer().isMoving(), 5000);
    }
}
```

#### Door fallback (e.g., "Pass-through")
```java
GameObject door = GameObjects.closest("Door");
if (door != null) {
    String action = door.hasAction("Open") ? "Open" : "Pass-through";
    if (door.interact(action)) {
        Sleep.sleepUntil(() -> !door.exists() || Players.localPlayer().isMoving(), 5000);
    }
}
```

// Resource gathering area
Area miningArea = new Area(3300, 3300, 3320, 3320, 0);
```

### World Hopping
```java
// Hop to a specific world
WorldHopper.hopWorld(301);

// Hop to a random world with filters
World randomWorld = Worlds.getRandomWorld(world -> 
    world != null && 
    world.getMinimumLevel() == 1 && 
    !world.isMembersOnly()
);
if (randomWorld != null) {
    WorldHopper.hopWorld(randomWorld);
}
```

### Grand Exchange Trading
```java
// Open the Grand Exchange
if (GrandExchange.isOpen() || GrandExchange.open()) {
    // Buy items
    if (GrandExchange.buyItem("Lobster", 10, 200)) {
        Sleep.sleepUntil(() -> GrandExchange.getFirstEmptySlot() == -1, 30000);
    }
    
    // Sell items
    if (GrandExchange.sellItem("Raw lobster", 10, 100)) {
        Sleep.sleepUntil(() -> GrandExchange.getFirstEmptySlot() == -1, 30000);
    }
    
    // Collect completed offers
    GrandExchange.collect();
}
```

### Widget Interactions
```java
// Get a widget by parent and child ID
WidgetChild leatherWidget = Widgets.getWidgetChild(270, 15);
if (leatherWidget != null && leatherWidget.interact()) {
    Sleep.sleepUntil(() -> !Inventory.contains("Leather") || !Inventory.contains("Thread"), 30000);
}

// Click a specific button in a widget
WidgetChild makeButton = Widgets.getWidgetChild(270, 14);
if (makeButton != null && makeButton.interact("Make")) {
    Sleep.sleepUntil(() -> !Inventory.contains("Leather"), 10000);
}

// Check if a widget is visible before interacting
WidgetChild bankWidget = Widgets.getWidgetChild(12, 2);
if (bankWidget != null && bankWidget.isVisible()) {
    bankWidget.interact("Close");
}

// Get widget text for decision making
WidgetChild pointsWidget = Widgets.getWidgetChild(122, 5);
if (pointsWidget != null) {
    String pointsText = pointsWidget.getText();
    Logger.log("Wintertodt points: " + pointsText);
}

// Interact with widgets in a sequence
WidgetChild firstOption = Widgets.getWidgetChild(219, 1);
WidgetChild secondOption = Widgets.getWidgetChild(219, 2);
if (firstOption != null && firstOption.interact()) {
    Sleep.sleep(1000);
    if (secondOption != null && secondOption.interact()) {
        Sleep.sleepUntil(() -> Dialogues.inDialogue(), 5000);
    }
}
```

### PlayerSettings (Varps and Varbits)
```java
// Get attack style (Varp 43)
int currentAttackStyle = PlayerSettings.getConfig(43);

// Get auto-cast spell (Varbit 276)
int currentAutoCastSpell = PlayerSettings.getBitValue(276);

// Check cannon ammo (Varp 3)
int currentAmmoCount = PlayerSettings.getConfig(3);
if (currentAmmoCount <= 10) {
    // Refill cannon
    GameObject cannon = GameObjects.closest("Dwarf multicannon");
    if (cannon != null && cannon.interact("Fire")) {
        Sleep.sleepUntil(() -> PlayerSettings.getConfig(3) > 10, 30000);
    }
}
```

#### How to Check Current Varp Values
Varps (VarPlayers) store game state information and can be checked using `PlayerSettings.getConfig(varpId)`. Use DreamBot's Game Explorer to find varp IDs and their current values.

##### Basic Varp Checking
```java
// Check tutorial progress (Varp 281)
int tutorialProgress = PlayerSettings.getConfig(281);
Logger.log("Current tutorial progress: " + tutorialProgress);

// Check combat style (Varp 43)
int attackStyle = PlayerSettings.getConfig(43);
switch (attackStyle) {
    case 0: Logger.log("Attack style: Stab"); break;
    case 1: Logger.log("Attack style: Lunge"); break;
    case 2: Logger.log("Attack style: Slash"); break;
    case 3: Logger.log("Attack style: Block"); break;
    default: Logger.log("Unknown attack style: " + attackStyle);
}

// Check prayer points (Varp 2382)
int prayerPoints = PlayerSettings.getConfig(2382);
if (prayerPoints < 10) {
    Logger.log("Low prayer points: " + prayerPoints);
}
```

##### Finding Varp IDs in Game Explorer
1. **Open Game Explorer**: Press `CTRL + G` in DreamBot client
2. **Go to Player Settings tab**: Shows all current varp values
3. **Left pane**: "Settings" tab shows varps, "Varbits" tab shows varbits
4. **Right pane**: "Recent Updates" shows varp changes when you perform actions
5. **Note the ID**: The number in the left column is the varp ID

##### Common Varp IDs to Check
```java
// Tutorial Island progress
int progress = PlayerSettings.getConfig(281);

// Combat settings
int attackStyle = PlayerSettings.getConfig(43);      // 0=Stab, 1=Lunge, 2=Slash, 3=Block
int strengthStyle = PlayerSettings.getConfig(44);    // Similar values
int defenceStyle = PlayerSettings.getConfig(45);     // Similar values

// Prayer points
int prayerPoints = PlayerSettings.getConfig(2382);

// Run energy
int runEnergy = PlayerSettings.getConfig(173);       // 0-10000 (divide by 100 for percentage)

// Special attack energy
int specialEnergy = PlayerSettings.getConfig(300);   // 0-1000 (divide by 10 for percentage)

// Quest points
int questPoints = PlayerSettings.getConfig(101);

// Music volume
int musicVolume = PlayerSettings.getConfig(168);     // 0-255
```

##### Using Varp Values in Conditions
```java
// Wait for tutorial progress to change
int startProgress = PlayerSettings.getConfig(281);
Sleep.sleepUntil(() -> PlayerSettings.getConfig(281) != startProgress, 30000);

// Check if player has enough prayer points for a spell
if (PlayerSettings.getConfig(2382) >= 10) {
    // Cast prayer spell
}

// Monitor run energy before long walks
if (PlayerSettings.getConfig(173) < 500) { // Less than 5% energy
    // Drink energy potion or rest
}
```

#### Tracking Varp Values Over Time
```java
// Store previous value to detect changes
private int previousTutorialProgress = -1;

@Override
public int onLoop() {
    int currentProgress = PlayerSettings.getConfig(281);
    
    // Check if progress has changed
    if (currentProgress != previousTutorialProgress) {
        Logger.log("Tutorial progress changed: " + previousTutorialProgress + " -> " + currentProgress);
        previousTutorialProgress = currentProgress;
    }
    
    // Use the progress value in your logic
    switch (currentProgress) {
        case 0: return talkToGuide();
        case 10: return leaveHouse();
        // ... more cases
    }
    
    return 1000;
}
```

#### Monitoring Multiple Varps for State Changes
```java
// Track multiple varps in a map
private Map<Integer, Integer> varpHistory = new HashMap<>();

private void checkVarpChanges() {
    // List of varps to monitor
    int[] varpsToTrack = {281, 43, 276}; // Tutorial progress, attack style, auto-cast
    
    for (int varpId : varpsToTrack) {
        int currentValue = PlayerSettings.getConfig(varpId);
        int previousValue = varpHistory.getOrDefault(varpId, -1);
        
        if (currentValue != previousValue) {
            Logger.log("Varp " + varpId + " changed: " + previousValue + " -> " + currentValue);
            varpHistory.put(varpId, currentValue);
            
            // React to specific changes
            if (varpId == 43 && currentValue == 1) {
                Logger.log("Player switched to Lunge attack style!");
            }
        }
    }
}
```

#### Quest Progress Tracking
```java
// Track quest progress over time
private int lastQuestStage = -1;

private void monitorQuestProgress(int questVarpId) {
    int currentStage = PlayerSettings.getBitValue(questVarpId);
    
    if (currentStage != lastQuestStage) {
        Logger.log("Quest stage changed: " + lastQuestStage + " -> " + currentStage);
        
        // Handle quest completion
        if (currentStage == 8) { // Assuming 8 is complete
            Logger.log("Quest completed!");
            // Perform completion actions
        }
        
        lastQuestStage = currentStage;
    }
}

// Usage in onLoop
@Override
public int onLoop() {
    monitorQuestProgress(8063); // X Marks the Spot quest varbit
    checkVarpChanges();
    
    // Rest of script logic
    return 1000;
}
```

**Simple Example**: Walking to a specific coordinate
```java
Tile targetTile = new Tile(3100, 3100);
if (Walking.shouldWalk(5)) { // Check if we can walk (not in combat, etc.)
    Walking.walk(targetTile);
}
```

**Complex Example**: Pathfinding with obstacles
```java
// For more complex navigation, you might need to handle obstacles
GameObject door = GameObjects.closest("Door");
if (door != null && door.distance() > 3) {
    Walking.walk(door);
    Sleep.sleepUntil(() -> door.distance() <= 3, 5000);
    door.interact("Open");
}
```

---

## 4. Inventory Management
Managing items is essential for crafting, cooking, and other tutorial activities.

### Basic Inventory Checks
```java
// Check if you have specific items
if (Inventory.contains("Pot of flour", "Bucket of water")) {
    // You have both items
}

// Check for any of several items
if (Inventory.containsAny("Bronze sword", "Bronze axe")) {
    // You have at least one weapon
}
```

### Item Interactions
```java
// Use one item on another
if (Inventory.interact("Pot of flour", "Use")) {
    if (Inventory.interact("Bucket of water", "Use")) {
        Sleep.sleepUntil(() -> Inventory.contains("Bread dough"), 3000);
    }
}
```

### Using an Inventory Item on a Game Object
To use an item from your inventory on a game object (e.g., cooking raw shrimps on a fire), you need to:
1. Call `interact("Use")` on the inventory item to select it
2. Then call `interact("Use")` on the game object

```java
// Use Raw shrimps (ID 2514) on a Fire (GameObject ID 26185)
Item rawShrimps = Inventory.get(2514);
GameObject fire = GameObjects.closest(26185);

if (rawShrimps != null && fire != null) {
    if (rawShrimps.interact("Use")) {
        Sleep.sleep(600);
        if (fire.interact("Use")) {
            // Wait until shrimps are cooked (no longer in inventory)
            Sleep.sleepUntil(() -> !Inventory.contains(2514), 10000);
        }
    }
}
```

**Required imports:**
```java
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Sleep;
```

### Dropping Items
```java
// Drop all items except those matching a filter
Inventory.dropAll(item -> item != null && item.getName().contains("logs"));

// Drop specific items by name
Inventory.drop("Raw fish");
Inventory.drop("Burnt fish");
```

**Simple Example**: Count items
```java
int coinCount = Inventory.count("Coins");
Logger.log("I have " + coinCount + " coins");
```

**Complex Example**: Inventory organization
```java
// Sort inventory by item ID
List<Item> sortedItems = Inventory.all().stream()
    .sorted(Comparator.comparing(Item::getID))
    .collect(Collectors.toList());

// Use items in a specific order
for (Item item : sortedItems) {
    if (item.getName().equals("Raw fish")) {
        item.interact("Cook");
        break;
    }
}
```

### Banking Operations
```java
// Open the bank
if (Bank.open()) {
    Sleep.sleepUntil(Bank::isOpen, 5000);
    
    // Deposit all items
    if (Bank.depositAllItems()) {
        Sleep.sleepUntil(() -> Inventory.isEmpty(), 3000);
    }
    
    // Deposit specific items
    Bank.depositAll("Raw fish");
    Bank.deposit("Coins", 1000);
    
    // Withdraw items
    if (Bank.withdraw("Lobster", 10)) {
        Sleep.sleepUntil(() -> Inventory.contains("Lobster"), 3000);
    }
    
    // Close bank
    Bank.close();
}
```

---

## 5. Dialogue Handling
NPC conversations are a big part of Tutorial Island. Handle them smoothly to avoid getting stuck.

### Basic Dialogue Check
```java
if (Dialogues.inDialogue()) {
    // We're in a conversation
}
```

### Continuing Dialogues
```java
if (Dialogues.canContinue()) {
    Dialogues.clickContinue();
}
```

### Choosing Options
```java
if (Dialogues.areOptionsAvailable()) {
    Dialogues.chooseFirstOption("Yes", "Sure", "Okay");
}
```

**Simple Example**: Simple conversation handler
```java
if (Dialogues.inDialogue()) {
    if (Dialogues.canContinue()) {
        Dialogues.clickContinue();
    }
}
```

**Complex Example**: Advanced dialogue with multiple choices
```java
if (Dialogues.inDialogue()) {
    if (Dialogues.canContinue()) {
        Dialogues.clickContinue();
    } else if (Dialogues.areOptionsAvailable()) {
        String[] options = Dialogues.getOptions();
        for (String option : options) {
            if (option.contains("skill") || option.contains("guide")) {
                Dialogues.chooseOption(option);
                break;
            }
        }
    }
}
```

---

## 6. Progress Tracking (Varp 281)
Varp 281 tracks your tutorial progress. Use it to determine what step you're on.

### Reading Progress
```java
int progress = PlayerSettings.getConfig(281);
Logger.log("Current progress: " + progress);
```

### Switch Statement for Progress
```java
int progress = PlayerSettings.getConfig(281);
switch (progress) {
    case 0: talkToGuide(); break;
    case 10: leaveHouse(); break;
    case 20: fishAtSpot(); break;
    case 30: cookFish(); break;
    // Add more cases as needed
    default: Logger.log("Unknown progress: " + progress);
}
```

**Simple Example**: Basic progress check
```java
if (PlayerSettings.getConfig(281) == 0) {
    // Talk to guide
}
```

**Complex Example**: Progress-based state machine
```java
public int onLoop() {
    int progress = PlayerSettings.getConfig(281);
    
    switch (progress) {
        case 0:
            return talkToGuide();
        case 10:
            return navigateToSurvivalExpert();
        case 20:
            return fishShrimp();
        case 30:
            return cookShrimp();
        // ... more cases
        default:
            Logger.log("Tutorial complete or unknown state: " + progress);
            return 1000;
    }
}

private int talkToGuide() {
    NPC guide = NPCs.closest("Gielinor Guide");
    if (guide != null && guide.interact("Talk-to")) {
        Sleep.sleepUntil(() -> Dialogues.inDialogue(), 5000);
    }
    return Calculations.random(800, 1200);
}
```

---

## 7. Mouse & Anti-Ban Philosophy
To avoid detection, make your script behave like a human player.

### Human-like Delays
```java
// Instead of fixed delays, use random ranges
Sleep.sleep(Calculations.random(1000, 2000));
```

### Gaussian Distribution
```java
// More natural timing with bell curve
long delay = (long) Calculations.nextGaussian(1500, 250);
Sleep.sleep(delay);
```

### Micro-Actions
```java
// Occasionally perform human-like behaviors
if (Calculations.random(1, 100) < 5) { // 5% chance
    // Rotate camera slightly
    Camera.rotateTo(Calculations.random(0, 360));
}

// Check random skill occasionally
if (Calculations.random(1, 100) < 3) {
    Skills.open();
    Sleep.sleep(500, 1000);
    Skills.close();
}
```

### Vary Your Patterns
```java
// Don't always walk to the exact same spot
Area targetArea = new Area(3200, 3200, 3210, 3210);
Walking.walk(targetArea.getRandomTile());
```

**Simple Example**: Basic random delay
```java
// Wait between 1-3 seconds
Sleep.sleep(Calculations.random(1000, 3000));
```

**Complex Example**: Advanced anti-ban routine
```java
private void performAntiBan() {
    int action = Calculations.random(1, 100);
    
    if (action <= 10) { // 10% chance
        // Move mouse randomly
        Mouse.moveRandomly();
    } else if (action <= 20) { // 10% chance
        // Rotate camera
        Camera.rotateTo(Calculations.random(0, 360));
    } else if (action <= 25) { // 5% chance
        // Check skills
        Skills.open();
        Sleep.sleep(1000, 2000);
        Skills.close();
    } else if (action <= 30) { // 5% chance
        // Move mouse off screen briefly
        Mouse.moveMouseOutsideScreen();
        Sleep.sleep(500, 1500);
    }
    // 70% chance: do nothing (normal behavior)
}
```

---

## 8. Data Recording Template
Use this to collect all your IDs during your manual run-through.

| Varp 281 | Task | Entity Type | Entity Name | ID | Parent ID | Action | Child ID | Tile (X,Y) | Area | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **0** | Talk to Guide | NPC | Gielinor Guide | 3308 | N/A | Talk-to | N/A | (3094, 3107) | Lumbridge Castle |  |
| **2** | Open Options | Widget | Settings Interface | 548 | N/A | Click | 45 | N/A | N/A |  |

---

## 9. Putting It All Together: A Complete Example
Here's a simplified but complete Tutorial Island script that demonstrates all the concepts we've covered. This script handles the first few steps of the tutorial.

```java
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Calculations;

@ScriptManifest(
    name = "Tutorial Island Script",
    author = "DreamBot Guide",
    description = "Completes Tutorial Island",
    category = Category.MISC,
    version = 1.0
)
public class TutorialScript extends AbstractScript {

    @Override
    public void onStart() {
        Logger.log("Tutorial Island script started!");
    }

    @Override
    public int onLoop() {
        int progress = PlayerSettings.getConfig(281);
        
        switch (progress) {
            case 0:
                return talkToGuide();
            case 2:
                return openSettings();
            case 10:
                return leaveHouse();
            default:
                Logger.log("Progress: " + progress + " - Script complete or unknown state");
                return 5000;
        }
    }

    private int talkToGuide() {
        NPC guide = NPCs.closest("Gielinor Guide");
        if (guide != null) {
            if (guide.distance() > 5) {
                if (Walking.shouldWalk()) {
                    Walking.walk(guide);
                }
            } else {
                if (guide.interact("Talk-to")) {
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), 5000);
                }
            }
        }
        return Calculations.random(800, 1200);
    }

    private int openSettings() {
        // This would interact with the settings widget
        // For simplicity, we'll just wait
        Logger.log("Opening settings...");
        return Calculations.random(1000, 2000);
    }

    private int leaveHouse() {
        // Walk out of the starting house
        if (Walking.shouldWalk()) {
            Walking.walk(new Tile(3098, 3107));
        }
        return Calculations.random(1000, 1500);
    }

    @Override
    public void onExit() {
        Logger.log("Tutorial Island script stopped!");
    }
}
```

This example shows how all the pieces fit together. In a real script, you'd expand each method with proper error checking, anti-ban measures, and complete the entire tutorial.

---

## 10. Troubleshooting Common Issues

### GUI Performance Issues
If your script's GUI causes performance issues, you're likely creating Swing components or invoking methods from the wrong thread. Swing is not thread-safe.

**Solution**: Use `SwingUtilities.invokeLater()` for asynchronous GUI updates:
```java
SwingUtilities.invokeLater(() -> {
    MyGUI gui = new MyGUI();
    gui.setVisible(true);
});
```

For immediate results, use `SwingUtilities.invokeAndWait()`, but avoid heavy processing on the EDT.

**Important**: Never perform heavy processing on the Event Dispatch Thread (EDT) as it will freeze the entire client.

### Script Not Loading
- Verify Java 11 is set as the active runtime in your IDE
- Check that the DreamBot BotData/repository2/ path is correct
- Ensure your main class name matches exactly in the manifest
- Confirm all imports are available in the DreamBot API

### Compilation Errors
- Make sure you're using Java 11 language features
- Verify all DreamBot API classes are imported correctly
- Check for typos in method names and class references

### Runtime Errors
- Use `Logger.log()` extensively to debug your script
- Test with DreamBot's developer tools open (CTRL+D, CTRL+G)
- Check the DreamBot console for detailed error messages

> [!TIP]
> **Final Recommendation**: Keep this guide open in VS Code. Every time you finish a new section of your script, test it multiple times in the DreamBot client to ensure it's stable!
