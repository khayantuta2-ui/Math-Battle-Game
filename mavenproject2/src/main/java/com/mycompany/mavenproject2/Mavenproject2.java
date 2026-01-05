package com.mycompany.mavenproject2;

import java.util.*;
import java.io.*;


//Main class//
public class Mavenproject2 {
    public static void main(String[] args) { 
        try (Scanner scanner = new Scanner(System.in)) { //Using the try si it closes once its done
            Mathorrior game = new Mathorrior(scanner); //Creating a game as soon as it starts
            game.startGame();
        }
    }
}

// For different types of attack
enum AttackType {
    BASIC_STRIKE, //Always works
    PRIME_STRIKE, //Only works if the enemy health is a prime Number
    MODULUS_STRIKE //Works if the enemy's health is divisible by 3
}

// Abstract Enemy class
abstract class Enemy {
    protected int health; //Enemy's health
    protected String name; //Enemy's name

    public Enemy(String name, int health) {
        this.name = name;
        this.health = health;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }
    
    public void reduceHealth(int damage) { //Reduces enemy health 
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    public boolean isAlive() { //Once it reaches 0, the enemy dies
        return health > 0;
    }

    // each enemy have their own weakness
    public abstract boolean isWeakTo(AttackType attackType);
}

// Player class
class User {
    private final String name;
    private int health;
    private int score;

    public User(String name) {
        this.name = name;
        this.health = 20;   //Player's health starts with 20 HP
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }

    public void reduceHealth(int damage) {
        this.health -= damage;
        if (this.health < 0) {
            this.health = 0;
        }
    }
    //Add points when successful
    public void increaseScore(int points) {
        this.score += points;
    }

    public boolean isAlive() {
        return health > 0;
    }
}


// Slime enemy class
class SlimeEnemy extends Enemy {
    public SlimeEnemy() {
        super("Slime", new Random().nextInt(10) + 15);  // HP between 15-24
    }

    @Override
    public boolean isWeakTo(AttackType attackType) {
        if (attackType == AttackType.MODULUS_STRIKE) {  //Slime is too weak to do a MODULUS Srike whenit's divisible by 3
            return health % 3 == 0;
        }
        return false;
    }
}

// Goblin enemy class
class GoblinEnemy extends Enemy {
    public GoblinEnemy() {
        super("Goblin", new Random().nextInt(10) + 15); // HP between 15-24
    }

    @Override
    public boolean isWeakTo(AttackType attackType) {
        if (attackType == AttackType.PRIME_STRIKE) {  //Goblins is weak if PRIME STRIKE is used when the health is a prime number
            return isPrime(health);
        }
        return false;
    }

    private boolean isPrime(int n) {   // check if a number is prime
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;

        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }
}

// Attack class
class Attack {
    private final String name;
    private final AttackType type;
    
    private final int power;  //How much HP it takes out

    public Attack(String name, AttackType type, int power) {
        this.name = name;
        this.type = type;
        this.power = power;
    }

    public String getName() {
        return name;
    }

    public AttackType getType() {
        return type;
    }

    public int getPower() {
        return power;
    }
}

// Game class to manage the game logic
class Mathorrior {
    private User user;  //The player
    private Enemy enemy;    //The current enemy
    private List<Attack> attacks; //Player's attack options
    private final Random random;    //For randomness like, enemy HP, attack damage...etc)
    private final Scanner scanner;  //For user input
    private static final String SCORE_FILE = "player_scores.txt";   //Stores scores


    public Mathorrior(Scanner scanner) {
        this.random = new Random();
        this.scanner = scanner;
        initializeAttacks(); //Sets up the attack list
    }
    //Initialize available attacks
    private void initializeAttacks() {
        attacks = new ArrayList<>();
        attacks.add(new Attack("Basic Strike", AttackType.BASIC_STRIKE, 5));
        attacks.add(new Attack("Prime Strike", AttackType.PRIME_STRIKE, 10));
        attacks.add(new Attack("Modulus Strike", AttackType.MODULUS_STRIKE, 10));
    }

    public void startGame() {       
               
        // Get player name with validation
        String userName = getValidUserName();   //Get Player name
        user = new User(userName);

        // Selects the random enemy
        selectRandomEnemy();

        // Main game loop
        gameLoop(); //Runs the game until someone dies

        // Saves the score
        saveScore();
    }
    
    //Ensures player enters a valid name(Chacters and strings only)
    private String getValidUserName() {
        String name;

        while (true) {
            System.out.println("Mathorrior: Welcome to the battle of Math!!");
            System.out.println("Enter your name: ");
            name = scanner.nextLine().trim();

            if (name.matches("[a-zA-Z]+")) {
                break;  //Stops it
            } else {
                System.out.println("Please End letters only.");
            }
        }
        return name;
    }

    private void selectRandomEnemy() {  //Choose between a Goblin or Slime
        int enemyType = random.nextInt(2);  //0 or 1
        if (enemyType == 0) {
            enemy = new SlimeEnemy();
        } else {
            enemy = new GoblinEnemy();
        }
        //Prints out the enemy's name and health
        System.out.println("\nA wild " + enemy.getName() + " appears! HP: " + enemy.getHealth());
    }
    //Game loops till either the player or enemy dies
    private void gameLoop() {
        while (user.isAlive() && enemy.isAlive()) {
            displayStatus();

            // Get player's the choice of attack
            int choice = getAttackChoice();
            Attack selectedAttack = attacks.get(choice - 1);

            // Process the attack
            processAttack(selectedAttack);

            // Enemy counterattack if it's alive
            if (enemy.isAlive()) {
                enemyCounterattack();
            }

            System.out.println();
        }

        // Game over message
        if (user.isAlive()) {
            System.out.println("You defeated the " + enemy.getName() + "! Victory!");
        } else {
            System.out.println("You were defeated. Game Over.");
        }
    }

    private void displayStatus() { //Shows the current player and enemy status
        System.out.println(user.getName() + " HP: " + user.getHealth() + " | Score: " + user.getScore());
        System.out.println(enemy.getName() + " HP: " + enemy.getHealth());
    }

    //Menu for attack selection
    private int getAttackChoice() {
        int choice = 0;

        while (true) {
            System.out.println("\nChoose your attack:");
            for (int i = 0; i < attacks.size(); i++) {
                System.out.println((i + 1) + ". " + attacks.get(i).getName());
            }

            System.out.print("Enter 1-3: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= 3) {
                    break;
                } else {
                    System.out.println("Invalid choice! Please enter a number between 1 and 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }
        return choice;
    }
    //Handles players attack logic
    private void processAttack(Attack attack) {
        boolean isEffective;
        isEffective = false;

        if (attack.getType() == AttackType.BASIC_STRIKE) {   // Check if attack is effective
            isEffective = true;
        } else {
            isEffective = enemy.isWeakTo(attack.getType());
        }

        // Processes the results
        if (isEffective) {
            enemy.reduceHealth(attack.getPower());
            user.increaseScore(10); //Reward for successful attack
            System.out.println(attack.getName() + " was effective!");
        } else {
            System.out.println(attack.getName() + " had no effect.");
        }
    }
        //The enemy does random damage between 3-6
    private void enemyCounterattack() {
        int damage = random.nextInt(4) + 3; // Random damage between 3-6
        user.reduceHealth(damage);
        System.out.println(enemy.getName() + " hit you for " + damage + " damage!");
    }
        //Saves the player's score
    private void saveScore() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORE_FILE, true))) {
            writer.println(user.getName() + ": " + user.getScore());
            System.out.println("Score saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving score: " + e.getMessage());
        }
    }
}

