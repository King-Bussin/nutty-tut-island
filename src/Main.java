import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.mouse.algorithm.StandardMouseAlgorithm;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.util.Random;
import javax.swing.JOptionPane;

@ScriptManifest(
    name = "Nutty Tutorial Island",
    author = "Nutmeg Dan",
    description = "Automatically completes Tutorial Island with human-like mouse movement, anti-ban, and adaptive timing. Start script on character customization screen.",
    category = Category.MISC,
    version = 1.0
)
public class Main extends AbstractScript {

    private Random random = new Random();
    private Area survivalArea = new Area(3105, 3093, 3100, 3098);
    private Area fishingArea = new Area(3101, 3097, 3104, 3094);
    private Area combatInstructorArea = new Area(3104, 9508, 3107, 9505);
    private long startTime;
    private String currentAction = "Starting...";
    private int antiBanCount = 0;
    private String lastAntiBan = "None";
    private long lastAntiBanTime = 0;
    private int lastVarp = 0;
    private long lastVarpChangeTime = 0;
    private HumanMouseAlgorithm mouseAlgo;
    private boolean varpJustChanged = false;
    private long completionTime = -1;
    private int ironmanMode = 0; // 0=normal, 1=ironman

    private static final int SCREEN_W = 765;
    private static final int SCREEN_H = 503;
    private static final int FINAL_VARP = 1000;
    private static final String[] STEP_NAMES = {
        "Character Creation", "Talk to Gielinor Guide", "Open Settings", "Talk to Guide Again",
        "Exit Door", "Walk to Survival Expert", "Open Inventory", "Fish Shrimps",
        "Open Skills", "Talk to Survival Expert", "Chop Tree", "Light Fire",
        "Cook Shrimps", "Walk to Kitchen", "Enter Kitchen", "Talk to Chef",
        "Make Dough", "Cook Bread", "Exit Kitchen", "Enable Run",
        "Run to Quest Guide", "Talk to Quest Guide", "Open Quest Tab", "Talk to Quest Guide",
        "Go Down Ladder", "Talk to Mining Instructor", "Continue Dialogue",
        "Mine Tin", "Mine Copper", "Smelt Bronze", "Talk to Instructor",
        "Use Anvil", "Smith Dagger", "Walk to Gate", "Talk to Combat Instructor",
        "Open Equipment", "More Info", "Equip Dagger", "Talk to Instructor",
        "Equip Sword+Shield", "Open Combat Tab", "Enter Rat Pen", "Kill Rat",
        "Exit Rat Pen", "Equip Ranged", "Kill Rat (Ranged)", "Climb Ladder",
        "Use Bank", "Close Bank + Poll", "Open Door", "Talk to Account Guide",
        "Open Account Tab", "Talk to Account Guide", "Walk Through Door",
        "Talk to Brother Brace", "Open Prayer Tab", "Talk to Brother Brace",
        "Leave Chapel", "Run to Magic Instructor", "Open Magic Tab",
        "Talk to Magic Instructor", "Cast Wind Strike", "Final Talk",
        "Home Teleport", "Complete!"
    };
    private static final int[] STEP_VARPS = {
        1, 2, 3, 7, 10, 20, 30, 40, 50, 60, 70, 80, 90,
        120, 130, 140, 150, 160, 170, 200, 210, 220, 230, 240,
        250, 260, 270, 300, 310, 320, 330, 340, 350, 360, 370,
        390, 400, 405, 410, 420, 430, 440, 460, 470, 480, 490, 500,
        510, 520, 525, 530, 531, 532, 540, 550, 560, 570,
        610, 620, 630, 640, 650, 671, 680, 1000
    };

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        lastVarpChangeTime = System.currentTimeMillis();
        mouseAlgo = new HumanMouseAlgorithm();
        Mouse.setMouseAlgorithm(mouseAlgo);
        int choice = JOptionPane.showConfirmDialog(null, "Enable Ironman mode?", "Nutty Tutorial Island",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        ironmanMode = (choice == JOptionPane.YES_OPTION) ? 1 : 0;
        Logger.log("Nutty Tutorial Island starting (mode: " + (ironmanMode == 1 ? "Ironman" : "Normal") + ")");
    }

    @Override
    public int onLoop() {
        int varp = PlayerSettings.getConfig(281);
        if (varp != lastVarp) {
            lastVarpChangeTime = System.currentTimeMillis();
            varpJustChanged = true;
        }
        lastVarp = varp;

        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] == varp) {
                currentAction = STEP_NAMES[i];
                break;
            }
        }

        // Stuck detection — if varp hasn't changed in 3 minutes, attempt recovery
        if (System.currentTimeMillis() - lastVarpChangeTime > 180000) {
            Logger.log("[WATCHDOG] Stuck for 3+ minutes at varp " + varp + ", attempting recovery...");
            currentAction = "Stuck recovery...";
            if (Dialogues.inDialogue()) {
                Dialogues.clickContinue();
                gaussianSleep(1000, 250, 350);
            }
            lastVarpChangeTime = System.currentTimeMillis();
        }

        // Periodic run energy check (run unlocked at varp 200+)
        if (varp >= 200 && !Walking.isRunEnabled() && Walking.getRunEnergy() > 20) {
            Walking.toggleRun();
            gaussianSleep(450, 100, 350);
        }

        // Variable reaction time after varp change
        if (varpJustChanged) {
            varpJustChanged = false;
            int reactionRoll = random.nextInt(100);
            if (reactionRoll < 25) {
                gaussianSleep(275, 50, 200);       // instant — was already watching
            } else if (reactionRoll < 55) {
                gaussianSleep(575, 120, 351);      // quick reaction
            } else if (reactionRoll < 85) {
                gaussianSleep(1150, 200, 801);     // normal reaction
            } else {
                gaussianSleep(2250, 400, 1501);    // slow — was looking away
            }
        }

        int abRoll = random.nextInt(1000);
        if (abRoll < 450) {
            performAntiBan();
        } else if (abRoll < 700) {
            mouseDrift();
        } else if (abRoll < 708) {
            shortAFK();
        }

        switch (varp) {

            case 1: // Character Creation
                return handleCharacterCreation();

            case 2: // Talk to Gielinor Guide
                preActionHesitation();
                NPC guide2 = NPCs.closest(3308);
                if (guide2 == null) break;
                if (!guide2.isOnScreen()) { Camera.rotateToEntity(guide2); gaussianSleep(700, 150, 350); }
                guide2.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 3: // Open Settings tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild settings3 = Widgets.get(164, 41);
                if (settings3 == null) break;
                settings3.interact();
                postActionSleep();
                break;

            case 7: // Talk to Gielinor Guide again
                preActionHesitation();
                NPC guide7 = NPCs.closest(3308);
                if (guide7 == null) break;
                if (!guide7.isOnScreen()) { Camera.rotateToEntity(guide7); gaussianSleep(700, 150, 350); }
                guide7.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 10: // Exit nearby door
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door10 = GameObjects.closest(9398);
                if (door10 != null) {
                    if (!door10.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door10.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 20: // Walk to Survival Instructor and talk
                preActionHesitation();
                long walkStart20 = System.currentTimeMillis();
                while (!survivalArea.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart20 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(survivalArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                NPC survival20 = NPCs.closest(8503);
                if (survival20 == null) break;
                if (!survival20.isOnScreen()) { Camera.rotateToEntity(survival20); gaussianSleep(700, 150, 350); }
                survival20.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 30: // Open Inventory tab
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild inv30 = Widgets.get(164, 55);
                if (inv30 == null) break;
                inv30.interact();
                postActionSleep();
                break;

            case 40: // Fish a shrimp
                preActionHesitation();
                NPC fishSpot40 = NPCs.closest(3317);
                if (fishSpot40 == null) {
                    if (!Players.getLocal().isMoving()) Walking.walk(fishingArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!fishSpot40.isOnScreen()) {
                    Camera.rotateToEntity(fishSpot40);
                    gaussianSleep(700, 150, 350);
                }
                fishSpot40.interact("Net");
                long fishStart40 = System.currentTimeMillis();
                while (Inventory.count("Raw shrimps") == 0 && PlayerSettings.getConfig(281) == varp && System.currentTimeMillis() - fishStart40 < 30000) {
                    gaussianSleep(1500, 400, 800);
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                        NPC respot40 = NPCs.closest(3317);
                        if (respot40 != null) {
                            if (!respot40.isOnScreen()) Camera.rotateToEntity(respot40);
                            respot40.interact("Net");
                        }
                        gaussianSleep(2000, 500, 1000);
                    }
                    if (random.nextInt(100) < 40) mouseDrift();
                    else if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 50: // Open Skills tab
                preActionHesitation();
                WidgetChild skills50 = Widgets.get(164, 53);
                if (skills50 == null) break;
                skills50.interact();
                postActionSleep();
                break;

            case 60: // Talk to Survival Instructor again
                preActionHesitation();
                NPC survival60 = NPCs.closest(8503);
                if (survival60 == null) break;
                if (!survival60.isOnScreen()) { Camera.rotateToEntity(survival60); gaussianSleep(700, 150, 350); }
                survival60.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 70: // Cut down a tree
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject tree70 = GameObjects.closest(9730);
                if (tree70 == null) break;
                tree70.interact("Chop down");
                long chopStart70 = System.currentTimeMillis();
                while (Inventory.count("Logs") == 0 && System.currentTimeMillis() - chopStart70 < 15000) {
                    gaussianSleep(3000, 500, 2000);
                    if (random.nextInt(100) < 40) mouseDrift();
                    performAntiBan();
                }
                postActionSleep();
                break;

            case 80: // Light the logs
                preActionHesitation();
                if (!Inventory.contains("Logs")) {
                    GameObject tree80 = GameObjects.closest(9730);
                    if (tree80 == null) break;
                    tree80.interact("Chop down");
                    Sleep.sleepUntil(() -> Inventory.contains("Logs"), 15000);
                    break;
                }
                Walking.walk(survivalArea.getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                if (Inventory.interact("Tinderbox", "Use")) {
                    if (Inventory.interact("Logs", "Use")) {
                        Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 5000);
                        gaussianSleep(6000, 800, 4000);
                    }
                }
                postActionSleep();
                break;

            case 90: // Cook the shrimp
                preActionHesitation();
                Item rawShrimps90 = Inventory.get(2514);
                GameObject fire90 = GameObjects.closest(26185);
                if (rawShrimps90 != null && fire90 != null) {
                    if (rawShrimps90.interact("Use")) {
                        gaussianSleep(650, 150, 350);
                        if (fire90.interact("Use")) {
                            Sleep.sleepUntil(() -> !Inventory.contains(2514), 10000);
                        }
                    }
                }
                postActionSleep();
                break;

            case 120: // Click continue and walk through next gate
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                Walking.walk(new Area(3090, 3094, 3093, 3091).getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                GameObject gate120 = GameObjects.closest(9470, 9708);
                if (gate120 != null) {
                    if (!gate120.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate120.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 130: // Walk closer to kitchen and go through door
                preActionHesitation();
                Walking.walk(new Area(3079, 3086, 3082, 3082).getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                GameObject door130 = GameObjects.closest(9709);
                if (door130 != null) {
                    if (!door130.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door130.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 140: // Talk to Cooking Instructor
                preActionHesitation();
                NPC chef140 = NPCs.closest(3305);
                if (chef140 == null) break;
                if (!chef140.isOnScreen()) { Camera.rotateToEntity(chef140); gaussianSleep(700, 150, 350); }
                chef140.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 150: // Mix flour and water to make dough
                preActionHesitation();
                for (int i = 0; i < 10 && Dialogues.inDialogue(); i++) {
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                if (Inventory.interact("Pot of flour", "Use")) {
                    gaussianSleep(650, 150, 350);
                    if (Inventory.interact("Bucket of water", "Use")) {
                        Sleep.sleepUntil(() -> Inventory.contains("Bread dough"), 5000);
                    }
                }
                postActionSleep();
                break;

            case 160: // Cook dough on range to make bread
                preActionHesitation();
                Item dough160 = Inventory.get("Bread dough");
                GameObject range160 = GameObjects.closest(9736);
                if (dough160 != null && range160 != null) {
                    dough160.interact("Use");
                    gaussianSleep(650, 150, 350);
                    range160.interact("Use");
                    Sleep.sleepUntil(() -> Inventory.contains("Bread"), 5000);
                }
                postActionSleep();
                break;

            case 170: // Exit the kitchen
                preActionHesitation();
                GameObject door170 = GameObjects.closest(9710);
                if (door170 != null) {
                    if (!door170.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door170.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 200: // Click run energy
                preActionHesitation();
                WidgetChild run200 = Widgets.get(160, 28);
                if (run200 == null) break;
                run200.interact();
                postActionSleep();
                break;

            case 210: // Run to Quest Guide
                preActionHesitation();
                if (!Walking.isRunEnabled() && Walking.getRunEnergy() > 10) {
                    Walking.toggleRun();
                    gaussianSleep(450, 100, 350);
                }
                Area questGuideArea = new Area(3088, 3126, 3082, 3128);
                long walkStart210 = System.currentTimeMillis();
                while (!questGuideArea.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart210 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(questGuideArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject door210 = GameObjects.closest(9716);
                if (door210 != null) {
                    if (!door210.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door210.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 220: // Talk to Quest Guide
                preActionHesitation();
                NPC quest220 = NPCs.closest(3312);
                if (quest220 == null) break;
                if (!quest220.isOnScreen()) { Camera.rotateToEntity(quest220); gaussianSleep(700, 150, 350); }
                quest220.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 230: // Open Quest tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild quest230 = Widgets.get(164, 54);
                if (quest230 == null) break;
                quest230.interact();
                postActionSleep();
                break;

            case 240: // Talk to Quest Guide again
                preActionHesitation();
                NPC quest240 = NPCs.closest(3312);
                if (quest240 == null) break;
                if (!quest240.isOnScreen()) { Camera.rotateToEntity(quest240); gaussianSleep(700, 150, 350); }
                quest240.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 250: // Go down the ladder
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject ladder250 = GameObjects.closest(9726);
                if (ladder250 != null) {
                    ladder250.interact("Climb-down");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 260: // Talk to mining instructor
                preActionHesitation();
                Area miningArea = new Area(3078, 9507, 3085, 9501);
                long walkStart260 = System.currentTimeMillis();
                while (!miningArea.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart260 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(miningArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                NPC miner260 = NPCs.closest(3311);
                if (miner260 == null) break;
                if (!miner260.isOnScreen()) { Camera.rotateToEntity(miner260); gaussianSleep(700, 150, 350); }
                miner260.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 270: // Continue mining instructor dialogue
                handleDialogue(varp);
                postActionSleep();
                break;

            case 300: // Mine tin ore
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject tinRock300 = GameObjects.closest(10080);
                if (tinRock300 == null) break;
                tinRock300.interact("Mine");
                long mineStart300 = System.currentTimeMillis();
                while (!Inventory.contains("Tin ore") && System.currentTimeMillis() - mineStart300 < 15000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 310: // Mine copper ore
                preActionHesitation();
                GameObject copperRock310 = GameObjects.closest(10079);
                if (copperRock310 == null) break;
                copperRock310.interact("Mine");
                long mineStart310 = System.currentTimeMillis();
                while (!Inventory.contains("Copper ore") && System.currentTimeMillis() - mineStart310 < 15000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 320: // Use furnace to smelt bronze bar
                preActionHesitation();
                GameObject furnace320 = GameObjects.closest(10082);
                if (furnace320 == null) break;
                furnace320.interact("Use");
                long smeltStart320 = System.currentTimeMillis();
                while (!Inventory.contains("Bronze bar") && System.currentTimeMillis() - smeltStart320 < 15000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 330: // Talk to mining instructor again
                preActionHesitation();
                NPC miner330 = NPCs.closest(3311);
                if (miner330 == null) break;
                if (!miner330.isOnScreen()) { Camera.rotateToEntity(miner330); gaussianSleep(700, 150, 350); }
                miner330.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 340: // Click the anvil
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject anvil340 = GameObjects.closest(2097);
                if (anvil340 != null) {
                    if (!anvil340.isOnScreen()) {
                        Camera.rotateToEntity(anvil340);
                        gaussianSleep(700, 150, 350);
                    }
                    anvil340.interact("Smith");
                    Sleep.sleepUntil(() -> Widgets.get(312, 9) != null && Widgets.get(312, 9).isVisible(), 3000);
                }
                postActionSleep();
                break;

            case 350: // Smith the bronze dagger
                preActionHesitation();
                WidgetChild smith350 = Widgets.get(312, 9);
                if (smith350 != null) {
                    smith350.interact("Smith");
                    Sleep.sleepUntil(() -> Inventory.contains("Bronze dagger"), 10000);
                }
                postActionSleep();
                break;

            case 360: // Move to next area and open gate
                preActionHesitation();
                Area smithingExit = new Area(3093, 9503, 3091, 9502);
                long walkStart360 = System.currentTimeMillis();
                while (!smithingExit.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart360 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(smithingExit.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject gate360 = GameObjects.closest(9717, 9718);
                if (gate360 != null) {
                    if (!gate360.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate360.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 370: // Talk to combat instructor
                preActionHesitation();
                NPC combat370 = NPCs.closest(3307);
                if (combat370 == null) {
                    Walking.walk(combatInstructorArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!combat370.isOnScreen()) {
                    Camera.rotateToEntity(combat370);
                    gaussianSleep(700, 150, 350);
                }
                combat370.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 390: // Open Equipment tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild equip390 = Widgets.get(164, 56);
                if (equip390 == null) break;
                equip390.interact();
                postActionSleep();
                break;

            case 400: // Equipped - More Info button
                preActionHesitation();
                WidgetChild info400 = Widgets.get(387, 1);
                if (info400 == null) break;
                info400.interact();
                postActionSleep();
                break;

            case 405: // Equip the bronze dagger
                Inventory.interact(1205, "Equip");
                Sleep.sleepUntil(() -> Equipment.contains(1205), 5000);
                postActionSleep();
                break;

            case 410: // Close interface and talk to Combat Instructor again
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(84, 3, 0) != null && Widgets.get(84, 3, 0).isVisible()) {
                    Widgets.get(84, 3, 11).interact();
                    dialoguePause();
                }
                NPC combat410 = NPCs.closest(3307);
                if (combat410 == null) {
                    Walking.walk(combatInstructorArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!combat410.isOnScreen()) {
                    Camera.rotateToEntity(combat410);
                    gaussianSleep(700, 150, 350);
                }
                if (!combat410.isOnScreen()) {
                    Walking.walk(combatInstructorArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                combat410.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 5000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 420: // Equip bronze sword and shield
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                Inventory.interact(1277, "Wield");
                gaussianSleep(650, 150, 350);
                Inventory.interact(1171, "Wield");
                Sleep.sleepUntil(() -> Equipment.contains(1277) && Equipment.contains(1171), 5000);
                postActionSleep();
                break;

            case 430: // Open Combat tab
                preActionHesitation();
                WidgetChild combat430 = Widgets.get(164, 52);
                if (combat430 == null) break;
                combat430.interact();
                postActionSleep();
                break;

            case 440: // Go in rat pen and attack a rat
                preActionHesitation();
                Area ratPen = new Area(3111, 9520, 3113, 9517);
                long walkStart440 = System.currentTimeMillis();
                while (!ratPen.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart440 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(ratPen.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject gate440 = GameObjects.closest(9720);
                if (gate440 != null) {
                    if (!gate440.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate440.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 6000);
                    gaussianSleep(700, 150, 350);
                }
                NPC rat440 = NPCs.closest(3313);
                if (rat440 == null) break;
                rat440.interact("Attack");
                Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                postActionSleep();
                break;

            case 460: // Attack rat inside pen
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                if (!Players.getLocal().isInCombat()) {
                    NPC rat460 = NPCs.closest(3313);
                    if (rat460 == null) break;
                    rat460.interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                }
                if (Players.getLocal().isInCombat()) {
                    Sleep.sleepUntil(() -> !Players.getLocal().isInCombat() || PlayerSettings.getConfig(281) != varp, 15000);
                }
                postActionSleep();
                break;

            case 470: // Pass back through gate and talk to Combat Instructor
                preActionHesitation();
                GameObject gate470 = GameObjects.closest(9720);
                if (gate470 != null) {
                    gate470.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 6000);
                    gaussianSleep(700, 150, 350);
                }
                NPC combat470 = NPCs.closest(3307);
                if (combat470 == null) {
                    Walking.walk(combatInstructorArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!combat470.isOnScreen()) {
                    Camera.rotateToEntity(combat470);
                    gaussianSleep(700, 150, 350);
                }
                combat470.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 480: // Equip ranged gear and attack a rat
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (!Equipment.contains(841)) {
                    Inventory.interact(841, "Wield");
                    Sleep.sleepUntil(() -> Equipment.contains(841), 3000);
                    gaussianSleep(400, 100, 350);
                }
                if (!Equipment.contains(882)) {
                    Inventory.interact(882, "Wield");
                    Sleep.sleepUntil(() -> Equipment.contains(882), 3000);
                    gaussianSleep(400, 100, 350);
                }
                if (!Equipment.contains(882)) {
                    Inventory.interact(882, "Wield");
                    Sleep.sleepUntil(() -> Equipment.contains(882), 3000);
                }
                NPC rat480 = NPCs.closest(3313);
                if (rat480 == null) break;
                rat480.interact("Attack");
                long combatStart480 = System.currentTimeMillis();
                while (!Players.getLocal().isInCombat() && System.currentTimeMillis() - combatStart480 < 12000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 25) mouseDrift();
                }
                postActionSleep();
                break;

            case 490: // Kill rat with ranged
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                if (!Players.getLocal().isInCombat()) {
                    if (!Equipment.contains(841)) {
                        Inventory.interact(841, "Wield");
                        Sleep.sleepUntil(() -> Equipment.contains(841), 3000);
                        gaussianSleep(400, 100, 350);
                    }
                    if (!Equipment.contains(882)) {
                        Inventory.interact(882, "Wield");
                        Sleep.sleepUntil(() -> Equipment.contains(882), 3000);
                        gaussianSleep(400, 100, 350);
                    }
                    NPC rat490 = NPCs.closest(3313);
                    if (rat490 == null) break;
                    rat490.interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                }
                if (Players.getLocal().isInCombat()) {
                    Sleep.sleepUntil(() -> !Players.getLocal().isInCombat() || PlayerSettings.getConfig(281) != varp, 15000);
                }
                postActionSleep();
                break;

            case 500: // Travel up ladder
                preActionHesitation();
                Area ladderArea = new Area(3110, 9528, 3112, 9522);
                long walkStart500 = System.currentTimeMillis();
                while (!ladderArea.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart500 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(ladderArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject ladder500 = GameObjects.closest(9727);
                if (ladder500 != null) {
                    ladder500.interact("Climb-up");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 510: // Run inside bank area and click bank booth
                preActionHesitation();
                Area bankArea = new Area(3119, 3124, 3124, 3120);
                long walkStart510 = System.currentTimeMillis();
                while (!bankArea.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart510 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(bankArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject booth510 = GameObjects.closest(10083);
                if (booth510 != null) {
                    booth510.interact("Use");
                    gaussianSleep(3000, 600, 1500);
                }
                postActionSleep();
                break;

            case 520: // Close bank interface then click poll booth
                if (Widgets.get(12, 2, 0) != null && Widgets.get(12, 2, 0).isVisible()) {
                    Widgets.get(12, 2, 11).interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject pollBooth520 = GameObjects.closest(26815);
                if (pollBooth520 != null) {
                    pollBooth520.interact("Use");
                    dialoguePause();
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                    handleDialogue(varp);
                }
                postActionSleep();
                break;

            case 525: // Close poll booth then open door to next area
                preActionHesitation();
                if (Widgets.get(928, 3, 0) != null && Widgets.get(928, 3, 0).isVisible()) {
                    Widgets.get(928, 4).interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door525 = GameObjects.closest(9721);
                if (door525 != null) {
                    if (!door525.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door525.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 530: // Talk to Account Guide
                preActionHesitation();
                NPC account530 = NPCs.closest(3310);
                if (account530 == null) break;
                if (!account530.isOnScreen()) { Camera.rotateToEntity(account530); gaussianSleep(700, 150, 350); }
                account530.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 531: // Open Account Management interface
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild acctTab531 = Widgets.get(164, 39);
                if (acctTab531 == null) break;
                acctTab531.interact();
                postActionSleep();
                break;

            case 532: // Talk to Account Guide again
                NPC account532 = NPCs.closest(3310);
                if (account532 == null) break;
                if (!account532.isOnScreen()) { Camera.rotateToEntity(account532); gaussianSleep(700, 150, 350); }
                account532.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 540: // Walk through next door
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door540 = GameObjects.closest(9722);
                if (door540 != null) {
                    if (!door540.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door540.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 550: // Run to chapel and talk to Brother Brace
                preActionHesitation();
                Area chapelArea = new Area(3122, 3108, 3127, 3105);
                long walkStart550 = System.currentTimeMillis();
                while (!chapelArea.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart550 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(chapelArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                NPC brace550 = NPCs.closest(3319);
                if (brace550 == null) break;
                if (!brace550.isOnScreen()) { Camera.rotateToEntity(brace550); gaussianSleep(700, 150, 350); }
                brace550.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 560: // Open Prayer tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild prayer560 = Widgets.get(164, 57);
                if (prayer560 == null) break;
                prayer560.interact();
                postActionSleep();
                break;

            case 570: // Talk to Brother Brace again
                preActionHesitation();
                NPC brace570 = NPCs.closest(3319);
                if (brace570 == null) break;
                if (!brace570.isOnScreen()) { Camera.rotateToEntity(brace570); gaussianSleep(700, 150, 350); }
                brace570.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 610: // Leave through door
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door610 = GameObjects.closest(9723);
                if (door610 != null) {
                    if (!door610.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door610.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 620: // Run to Magic Instructor and talk
                preActionHesitation();
                Area magicInstructorArea = new Area(3134, 3089, 3141, 3085);
                long walkStart620 = System.currentTimeMillis();
                while (!magicInstructorArea.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart620 < 30000) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(magicInstructorArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                NPC magic620 = NPCs.closest(3309);
                if (magic620 == null) break;
                if (!magic620.isOnScreen()) { Camera.rotateToEntity(magic620); gaussianSleep(700, 150, 350); }
                magic620.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 630: // Open Magic tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild magicTab630 = Widgets.get(164, 58);
                if (magicTab630 == null) break;
                magicTab630.interact();
                postActionSleep();
                break;

            case 640: // Talk to Magic Instructor again
                preActionHesitation();
                NPC magic640 = NPCs.closest(3309);
                if (magic640 == null) break;
                if (!magic640.isOnScreen()) { Camera.rotateToEntity(magic640); gaussianSleep(700, 150, 350); }
                magic640.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                handleDialogue(varp);
                postActionSleep();
                break;

            case 650: // Cast wind strike on a chicken
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild windStrike650 = Widgets.get(218, 12);
                if (windStrike650 != null && windStrike650.interact()) {
                    gaussianSleep(1000, 250, 350);
                    NPC chicken650 = NPCs.closest(3316);
                    if (chicken650 != null) {
                        chicken650.interact("Cast");
                        long castStart650 = System.currentTimeMillis();
                        while (!Players.getLocal().isInCombat() && System.currentTimeMillis() - castStart650 < 12000) {
                            gaussianSleep(2000, 500, 1000);
                            if (!Players.getLocal().isInCombat()) {
                                NPC rechicken650 = NPCs.closest(3316);
                                if (rechicken650 != null) {
                                    WidgetChild recast650 = Widgets.get(218, 12);
                                    if (recast650 != null) recast650.interact();
                                    gaussianSleep(1000, 250, 350);
                                    rechicken650.interact("Cast");
                                }
                            }
                        }
                    }
                }
                postActionSleep();
                break;

            case 671: // Close final interface and talk to Magic Instructor one last time
                preActionHesitation();
                if (Widgets.get(153, 16) != null && Widgets.get(153, 16).isVisible()) {
                    Widgets.get(153, 16).interact();
                    gaussianSleep(1000, 250, 350);
                }
                NPC magic671 = NPCs.closest(3309);
                if (magic671 == null) break;
                if (!magic671.isOnScreen()) { Camera.rotateToEntity(magic671); gaussianSleep(700, 150, 350); }
                magic671.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        String[] options = Dialogues.getOptions();
                        if (options != null && options.length > 0 && options[0] != null && options[0].toLowerCase().contains("ironman")) {
                            if (ironmanMode == 1) Dialogues.chooseOption(1);  // Ironman
                            else Dialogues.chooseOption(3);                   // Normal
                        } else {
                            Dialogues.chooseOption(1);
                        }
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 680: // Cast Home Teleport
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(218, 7) != null && Widgets.get(218, 7).interact()) {
                    gaussianSleep(700, 150, 350);
                    Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == 9599, 10000);
                    gaussianSleep(12000, 1500, 10000);
                }
                postActionSleep();
                break;

            case 1000: // Final dialogue
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                Logger.log("Tutorial Island completed!");
                currentAction = "Complete!";
                completionTime = System.currentTimeMillis() - startTime;
                break;

            default:
                Logger.log("Unhandled varp: " + varp);
                break;
        }

        return (int) Math.max(600 + random.nextGaussian() * 200, 350);
    }

    private void drawLogo(Graphics2D g2, int x, int y) {
        // Outer circle - dark bg with warm brown border
        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(x, y, 36, 36);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(180, 120, 50));
        g2.drawOval(x, y, 36, 36);

        // Acorn body (rounded bottom)
        g2.setColor(new Color(160, 100, 40));
        g2.fillOval(x + 9, y + 14, 18, 18);
        // Acorn highlight
        g2.setColor(new Color(200, 140, 60));
        g2.fillOval(x + 12, y + 17, 8, 10);

        // Acorn cap (top hat shape)
        g2.setColor(new Color(100, 70, 30));
        g2.fillRoundRect(x + 7, y + 11, 22, 8, 6, 6);
        // Cap texture lines
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(80, 55, 20));
        g2.drawLine(x + 12, y + 13, x + 12, y + 17);
        g2.drawLine(x + 18, y + 12, x + 18, y + 17);
        g2.drawLine(x + 24, y + 13, x + 24, y + 17);

        // Stem
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(100, 70, 30));
        g2.drawLine(x + 18, y + 11, x + 18, y + 5);
        // Small leaf on stem
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(0, 180, 70));
        g2.drawLine(x + 18, y + 7, x + 22, y + 4);
        g2.drawLine(x + 18, y + 7, x + 23, y + 7);
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        long elapsed = completionTime > 0 ? completionTime : System.currentTimeMillis() - startTime;
        String timeStr = String.format("%02d:%02d:%02d", elapsed / 3600000, (elapsed / 60000) % 60, (elapsed / 1000) % 60);

        int currentStep = 0;
        int totalSteps = STEP_VARPS.length;
        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] <= lastVarp) currentStep = i + 1;
        }
        double progress = (double) currentStep / totalSteps;
        int pct = (int)(progress * 100);

        int x = 6;
        int y = 152;
        int w = 258;
        int h = 186;
        int pad = 12;

        // ── PANEL ──
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(x + 3, y + 3, w, h, 14, 14);
        g2.setColor(new Color(10, 10, 10, 225));
        g2.fillRoundRect(x, y, w, h, 14, 14);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(180, 120, 50, 100));
        g2.drawRoundRect(x, y, w, h, 14, 14);

        // ── HEADER ──
        drawLogo(g2, x + pad - 2, y + 7);
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        g2.setColor(new Color(215, 155, 65));
        g2.drawString("NUTTY", x + 48, y + 23);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("Tutorial Island", x + 48, y + 35);
        g2.setColor(new Color(75, 75, 75));
        g2.drawString("by NutmegDan", x + 138, y + 35);

        // ── PROGRESS BAR (full-width accent) ──
        int barX = x;
        int barY = y + 44;
        int barW = w;
        int barH = 6;
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(barX, barY, barW, barH);
        if (progress > 0) {
            int fillW = Math.max((int)(barW * progress), 3);
            g2.setColor(new Color(0, 190, 80));
            g2.fillRect(barX, barY, fillW, barH);
            g2.setColor(new Color(0, 220, 100, 60));
            g2.fillRect(barX + fillW - 12, barY, 12, barH);
        }

        // ── PROGRESS TEXT ──
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(0, 210, 90));
        String pctStr = pct + "%";
        int pctW = g2.getFontMetrics().stringWidth(pctStr);
        g2.drawString(pctStr, x + pad, y + 78);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("Step " + currentStep + " / " + totalSteps, x + pad + pctW + 8, y + 78);

        // Time on the right
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(220, 220, 220));
        int timeW = g2.getFontMetrics().stringWidth(timeStr);
        g2.drawString(timeStr, x + w - pad - timeW, y + 76);

        // ── CURRENT ACTION (highlighted strip) ──
        g2.setColor(new Color(22, 22, 22));
        g2.fillRoundRect(x + pad - 2, y + 86, w - pad * 2 + 4, 20, 6, 6);
        g2.setColor(new Color(180, 120, 50, 40));
        g2.drawRoundRect(x + pad - 2, y + 86, w - pad * 2 + 4, 20, 6, 6);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(255, 255, 255));
        String displayAction = currentAction.length() > 30 ? currentAction.substring(0, 30) + ".." : currentAction;
        g2.drawString(displayAction, x + pad + 6, y + 100);

        // ── STATS TABLE ──
        int tableX = x + 6;
        int tableW = w - 12;
        int rowH = 18;
        int tableY = y + 112;
        int valX = x + 72;
        int col2LabelX = x + w / 2 + 6;
        int col2ValX = x + w / 2 + 42;

        // Row 1: Account + Anti-ban (darker bg)
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillRoundRect(tableX, tableY, tableW, rowH, 4, 4);

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("ACCOUNT", tableX + 8, tableY + 13);
        String modeStr = ironmanMode == 1 ? "Ironman" : "Normal";
        g2.setColor(ironmanMode == 1 ? new Color(220, 180, 120) : new Color(180, 180, 180));
        g2.drawString(modeStr, valX, tableY + 13);

        g2.setColor(new Color(140, 140, 140));
        g2.drawString("AB", col2LabelX, tableY + 13);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(255, 180, 50));
        g2.drawString(String.valueOf(antiBanCount), col2ValX, tableY + 13);

        // Row 2: Mouse + Varp (no bg, alternating)
        int row2Y = tableY + rowH + 2;

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("MOUSE", tableX + 8, row2Y + 13);
        String profileName = mouseAlgo != null ? mouseAlgo.getProfileName() : "Default";
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        if ("Slow".equals(profileName)) g2.setColor(new Color(100, 180, 255));
        else if ("Fast".equals(profileName)) g2.setColor(new Color(255, 140, 80));
        else g2.setColor(new Color(200, 200, 200));
        g2.drawString(profileName, valX, row2Y + 13);

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("VARP", col2LabelX, row2Y + 13);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(100, 150, 220));
        g2.drawString(String.valueOf(lastVarp), col2ValX, row2Y + 13);

        // Row 3: Last AB action (full width, subtle)
        int row3Y = row2Y + rowH + 2;
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillRoundRect(tableX, row3Y, tableW, rowH, 4, 4);

        if (lastAntiBanTime > 0) {
            long ago = (System.currentTimeMillis() - lastAntiBanTime) / 1000;
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.setColor(new Color(130, 130, 130));
            g2.drawString("LAST AB", tableX + 8, row3Y + 12);
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.setColor(new Color(100, 100, 100));
            String abDisplay = lastAntiBan + " (" + ago + "s ago)";
            if (abDisplay.length() > 26) abDisplay = abDisplay.substring(0, 26) + "..";
            g2.drawString(abDisplay, tableX + 56, row3Y + 12);
        } else {
            g2.setColor(new Color(55, 55, 55));
            g2.drawString("No anti-ban actions yet", tableX + 8, row3Y + 12);
        }
    }

    @Override
    public void onExit() {
        long elapsed = System.currentTimeMillis() - startTime;
        String timeStr = String.format("%02d:%02d:%02d", elapsed / 3600000, (elapsed / 60000) % 60, (elapsed / 1000) % 60);
        int currentStep = 0;
        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] <= lastVarp) currentStep = i + 1;
        }
        Logger.log("========================================");
        Logger.log("  Nutty Tutorial Island - Final Report");
        Logger.log("========================================");
        Logger.log("  Runtime:    " + timeStr);
        Logger.log("  Last Varp:  " + lastVarp);
        Logger.log("  Progress:   " + currentStep + "/" + STEP_VARPS.length + " (" + (int)((double) currentStep / STEP_VARPS.length * 100) + "%)");
        Logger.log("  Last Action: " + currentAction);
        Logger.log("  Anti-ban:   " + antiBanCount + " actions");
        if (lastVarp >= 1000) {
            Logger.log("  Status:     COMPLETED!");
        } else {
            Logger.log("  Status:     Stopped early");
        }
        Logger.log("========================================");
    }

    private void preActionHesitation() {
        int roll = random.nextInt(100);
        if (roll < 72) return;
        if (roll < 92) {
            gaussianSleep(800, 250, 400);
        } else {
            gaussianSleep(2000, 500, 1000);
        }
    }

    private void shortAFK() {
        logAntiBan("Short AFK");
        if (random.nextInt(100) < 50) {
            int side = random.nextInt(4);
            if (side == 0) Mouse.move(new Point(-5 - random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 1) Mouse.move(new Point(SCREEN_W + random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 2) Mouse.move(new Point(random.nextInt(SCREEN_W), -5 - random.nextInt(10)));
            else Mouse.move(new Point(random.nextInt(SCREEN_W), SCREEN_H + random.nextInt(10)));
            gaussianSleep(8000, 3000, 5000);
            Mouse.move(new Point(200 + random.nextInt(360), 100 + random.nextInt(250)));
        } else {
            gaussianSleep(6000, 2000, 4000);
        }
    }

    private void handleDialogue(int varp) {
        while (Dialogues.inDialogue()) {
            if (PlayerSettings.getConfig(281) != varp) break;
            if (Dialogues.canContinue()) {
                Dialogues.clickContinue();
                dialoguePause();
            } else if (Dialogues.getOptions() != null) {
                Dialogues.chooseOption(1);
                dialoguePause();
            } else {
                gaussianSleep(450, 80, 350);
            }
        }
    }

    private void dialoguePause() {
        int roll = random.nextInt(100);
        if (roll < 5) {
            gaussianSleep(2500, 600, 1200);  // really reading it carefully
        } else if (roll < 15) {
            gaussianSleep(1500, 350, 800);   // reading normally
        } else if (roll < 40) {
            gaussianSleep(800, 180, 450);    // skimming
        } else {
            gaussianSleep(500, 100, 350);    // spam-clicking through
        }
    }

    private double fatigueFactor() {
        long runtime = System.currentTimeMillis() - startTime;
        double minutes = runtime / 60000.0;
        return Math.min(1.0 + (minutes * 0.015), 1.2);
    }

    private void gaussianSleep(int mean, int stddev, int min) {
        int adjustedMean = (int)(mean * fatigueFactor());
        int delay = (int) (adjustedMean + random.nextGaussian() * stddev);
        Sleep.sleep(Math.max(delay, min));
    }

    private void postActionSleep() {
        postClickIdle();
        gaussianSleep(1200, 350, 350);
    }

    private void performAntiBan() {
        int roll = random.nextInt(100);
        if (roll < 10) {
            logAntiBan("Camera rotate");
            Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
        } else if (roll < 17) {
            logAntiBan("Camera nudge");
            Camera.rotateTo(Camera.getYaw() + random.nextInt(60) - 30, Camera.getPitch() + random.nextInt(20) - 10);
        } else if (roll < 27) {
            logAntiBan("Mouse jiggle");
            mouseJiggle();
        } else if (roll < 32) {
            logAntiBan("Mouse off screen");
            int side = random.nextInt(4);
            if (side == 0) Mouse.move(new Point(-5 - random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 1) Mouse.move(new Point(SCREEN_W + random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 2) Mouse.move(new Point(random.nextInt(SCREEN_W), -5 - random.nextInt(10)));
            else Mouse.move(new Point(random.nextInt(SCREEN_W), SCREEN_H + random.nextInt(10)));
            gaussianSleep(2500, 800, 1000);
            Mouse.move(new Point(200 + random.nextInt(360), 100 + random.nextInt(250)));
        }
    }

    private void logAntiBan(String action) {
        Logger.log("[ANTI-BAN] " + action);
        lastAntiBan = action;
        lastAntiBanTime = System.currentTimeMillis();
        antiBanCount++;
    }

    private void mouseJiggle() {
        Point pos = Mouse.getPosition();
        int dx = random.nextInt(30) - 15;
        int dy = random.nextInt(30) - 15;
        int newX = Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx));
        int newY = Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy));
        Mouse.move(new Point(newX, newY));
    }

    private void mouseDrift() {
        logAntiBan("Mouse drift");
        Point pos = Mouse.getPosition();
        int dx = random.nextInt(10) - 5;
        int dy = random.nextInt(10) - 5;
        int newX = Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx));
        int newY = Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy));
        Mouse.move(new Point(newX, newY));
    }

    private void postClickIdle() {
        int roll = random.nextInt(100);
        if (roll < 35) {
            // Small drift away from click position
            Point pos = Mouse.getPosition();
            int dx = random.nextInt(80) - 40;
            int dy = random.nextInt(80) - 40;
            int newX = Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx));
            int newY = Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy));
            gaussianSleep(400, 150, 350);
            Mouse.move(new Point(newX, newY));
        }
        // 65% - do nothing, leave mouse where it is
    }

    private int handleCharacterCreation() {
        // Phase 2: "How familiar are you?" screen
        if (Widgets.get(929, 7) != null && Widgets.get(929, 7).isVisible()) {
            Logger.log("Selecting experience level...");
            Widgets.get(929, 7).interact();
            gaussianSleep(750, 150, 350);
            Logger.log("Character creation completed!");
            return (int) Math.max(1200 + random.nextGaussian() * 300, 600);
        }

        // Phase 1: Wait for appearance screen to load
        if (Widgets.get(679, 74) == null || !Widgets.get(679, 74).isVisible()) {
            Logger.log("Waiting for character creation screen...");
            return (int) Math.max(700 + random.nextGaussian() * 150, 350);
        }

        Logger.log("Starting character creation...");

        int[][] designOptions = {
            {15, 16}, {19, 20}, {23, 24}, {27, 28}, {31, 32}, {35, 36}, {39, 40}
        };
        int[][] colourOptions = {
            {46, 47}, {50, 51}, {54, 55}, {58, 59}, {62, 63}
        };

        for (int[] option : designOptions) {
            if (random.nextInt(100) < 25) continue; // 25% chance to skip (leave default)
            int clicks = random.nextInt(5) + 1;
            for (int i = 0; i < clicks; i++) {
                Widgets.get(679, option[random.nextInt(2)]).interact();
                gaussianSleep(400, 80, 350);
            }
            if (random.nextInt(100) < 20) gaussianSleep(600, 200, 350); // occasional pause between options
        }

        for (int[] option : colourOptions) {
            if (random.nextInt(100) < 20) continue; // 20% chance to skip
            int clicks = random.nextInt(4) + 1;
            for (int i = 0; i < clicks; i++) {
                Widgets.get(679, option[random.nextInt(2)]).interact();
                gaussianSleep(400, 80, 350);
            }
            if (random.nextInt(100) < 20) gaussianSleep(600, 200, 350);
        }

        Widgets.get(679, 74).interact();
        Sleep.sleepUntil(() -> Widgets.get(929, 7) != null && Widgets.get(929, 7).isVisible(), 10000);

        return (int) Math.max(700 + random.nextGaussian() * 150, 350);
    }

    private static class HumanMouseAlgorithm extends StandardMouseAlgorithm {
        private final Random mouseRandom = new Random();
        private long lastProfileChange = System.currentTimeMillis();
        private int currentProfile = 1; // 0=slow, 1=normal, 2=fast
        private long profileDurationMs = 30000 + new Random().nextInt(60000);
        private double speedMultiplier = 1.0;

        private void maybeUpdateProfile() {
            if (System.currentTimeMillis() - lastProfileChange > profileDurationMs) {
                int roll = mouseRandom.nextInt(100);
                if (roll < 30) {
                    currentProfile = 0;
                    speedMultiplier = 0.7 + mouseRandom.nextDouble() * 0.15;
                } else if (roll < 75) {
                    currentProfile = 1;
                    speedMultiplier = 0.95 + mouseRandom.nextDouble() * 0.15;
                } else {
                    currentProfile = 2;
                    speedMultiplier = 1.2 + mouseRandom.nextDouble() * 0.3;
                }
                speedMultiplier += mouseRandom.nextGaussian() * 0.05;
                speedMultiplier = Math.max(0.5, Math.min(1.8, speedMultiplier));
                profileDurationMs = 15000 + mouseRandom.nextInt(75000);
                lastProfileChange = System.currentTimeMillis();
            }
        }

        @Override
        public double getMaxMagnitude() {
            maybeUpdateProfile();
            return Math.max(1.0, super.getMaxMagnitude() * speedMultiplier);
        }

        @Override
        public double getMinMagnitude() {
            maybeUpdateProfile();
            return Math.max(1.0, super.getMinMagnitude() * speedMultiplier);
        }

        @Override
        public double getAccelerationRate() {
            double noise = 0.9 + mouseRandom.nextDouble() * 0.2;
            return Math.max(1.0, super.getAccelerationRate() * speedMultiplier * noise);
        }

        @Override
        public double getMaxdTheta() {
            double inverseFactor = speedMultiplier > 0.01 ? 1.0 / speedMultiplier : 1.0;
            return Math.max(1.0, super.getMaxdTheta() * inverseFactor);
        }

        public String getProfileName() {
            if (currentProfile == 0) return "Slow";
            if (currentProfile == 2) return "Fast";
            return "Normal";
        }
    }
}
