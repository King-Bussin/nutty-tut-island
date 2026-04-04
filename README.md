# DreamBot Script Boilerplate for VS Code

This boilerplate helps you get started with DreamBot scripting in **Visual Studio Code**.

## Prerequisites
1.  **JDK 11**: DreamBot requires Java 11.
2.  **Extension Pack for Java**: Install this in VS Code (Ctrl+Shift+X, search "Extension Pack for Java").

## Project Setup
- The `src/Main.java` file is your script's main entry point. 
- The `.vscode/settings.json` file is configured to link your DreamBot `client.jar` automatically from `C:\Users\laver\DreamBot\BotData\repository2\client.jar`.

## How to Build and Export
To see your script in the DreamBot client, you need to compile it into a JAR and move it to your DreamBot scripts folder:

1.  **Open this folder**: Open `C:\Users\laver\.gemini\antigravity\scratch\dreambot-boilerplate` in VS Code.
2.  **Compile**: VS Code will show compile errors if the `client.jar` path in `.vscode/settings.json` is incorrect. If you see errors, check that path!
3.  **Export**: You can manually export a JAR or use a build tool like Maven. For now, you can copy the `.class` file or build a JAR. (In the future, I can help you set up a Maven `pom.xml` for easier builds).

For more advanced scripting, visit the [DreamBot Javadocs](https://dreambot.org/javadocs/).
