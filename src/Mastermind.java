import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.shape.Circle; 
import javafx.scene.layout.RowConstraints;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javafx.application.Platform;
import java.io.PrintWriter;
import java.net.InetSocketAddress;


public class Mastermind extends Application {

    Stage primaryStage;
    StackPane pane;
    Scene scene; 

    Label title; 
    boolean gameOver = false;

    PrintWriter out;

    int currentRow = 0;
    int currentCol = 0; 

    String[] colors = {"blue", "yellow", "red", "green"};
    String[] guess = new String[4];
    String[] colorCode = new String[4];
   
   
    Circle[][] boxes = new Circle[8][4];
    Circle[][] hintBoxes = new Circle[8][4];
    String[][] playerGuesses = new String[8][4];

    public void start (Stage stage) {

        colorCode = getColorCode();
        VBox rulesLayout = new VBox();
        rulesLayout.setSpacing(20);
        rulesLayout.setAlignment(Pos.CENTER);

        Label rulesTitle = new Label("Mastermind");
        rulesTitle.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        rulesLayout.getChildren().add(rulesTitle); 
        Label rules = new Label("Guess the secret color code. \n Press the phyiscal buttons infront of you to \nenter your guess.\n \n If the light is red it means\n there is a color in the code but in the wrong place. \n \n If the light is yellow it means \nthere is a color in the correct place.");
        rulesLayout.getChildren().add(rules);
        rules.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;-fx-text-alignment: center;");

        
        

    
    
    Button playBtn = new Button ("Play");
    rulesLayout.getChildren().add(playBtn); 
    rulesLayout.setStyle("-fx-background-color: #121213;");
    playBtn.setOnAction(event -> {
        stage.setScene(scene);
    });
    
    
primaryStage = stage; 
title = new Label("Mastermind"); 
title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

HBox colorOptions = new HBox();
colorOptions.setAlignment(Pos.CENTER);

GridPane grid = new GridPane();
grid.setHgap(5);
grid.setVgap(5); 
grid.setAlignment(Pos.CENTER);

GridPane hintGrid = new GridPane();
hintGrid.setHgap(5);
hintGrid.setVgap(5); 
hintGrid.setAlignment(Pos.CENTER);

for(int row = 0; row < 16; row++) {
    RowConstraints rc = new RowConstraints();
    if(row % 2 == 1) {
        rc.setMinHeight(27); // gap between pairs
    } else {
        rc.setMinHeight(5); // height within pair
    }
    hintGrid.getRowConstraints().add(rc);
}
 


for(int row= 0; row < 8; row++){ 
    for (int col = 0; col < 4; col++) {
        boxes[row][col] = new Circle(25);
        boxes[row][col].setFill(Color.GREY);
        grid.add(boxes[row][col], col, row);
    }
}

for(int row= 0; row < 8; row++){ 
    hintGrid.add(hintBoxes[row][0] = new Circle(10), 0, row*2);
    hintGrid.add(hintBoxes[row][1] = new Circle(10), 1, row*2);
    hintGrid.add(hintBoxes[row][2] = new Circle(10), 0, row*2+1);
    hintGrid.add(hintBoxes[row][3] = new Circle(10), 1, row*2+1);
    for(int col = 0; col < 4; col++){
        hintBoxes[row][col].setFill(Color.GREY);
    }
}

HBox gridsBox = new HBox();
gridsBox.setSpacing(30);
gridsBox.getChildren().add(grid);
gridsBox.getChildren().add(hintGrid);
gridsBox.setAlignment(Pos.CENTER);


 Button enter = new Button("enter");
enter.setOnAction(event -> handleEnter());

Button green = new Button("green"); 
green.setOnAction(event -> handleGreen());

 Button blue = new Button("blue"); 
blue.setOnAction(event -> handleBlue());

 Button yellow = new Button("yellow"); 
yellow.setOnAction(event -> handleYellow());

 Button red = new Button("red"); 
red.setOnAction(event -> handleRed());


colorOptions.getChildren().add(enter);
colorOptions.getChildren().add(green);
colorOptions.getChildren().add(blue);
colorOptions.getChildren().add(yellow);
colorOptions.getChildren().add(red);





VBox mainLayout = new VBox();

mainLayout.getChildren().add(title);
mainLayout.setAlignment(Pos.CENTER);
mainLayout.setSpacing(10);
 mainLayout.getChildren().add(gridsBox);
 mainLayout.getChildren().add(colorOptions);
pane = new StackPane (mainLayout);
pane.setStyle("-fx-background-color: #121518ff;");
pane.setFocusTraversable(true);
pane.requestFocus();
scene = new Scene(pane, 400, 600);
Scene rulesScene = new Scene(rulesLayout, 400, 600);

stage.setTitle("Mastermind");



//from claude to communicate with raspberry pi 
Thread socketThread = new Thread(() -> {
    try {
        ServerSocket serverSocket = new ServerSocket(5050);
        System.out.println("Waiting for Pi connection...");
        Socket socket = serverSocket.accept();
        System.out.println("Pi connected!");
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Received: " + message);
            switch(message.trim()) {
                case "green": Platform.runLater(() -> handleGreen()); break;
                case "blue": Platform.runLater(() -> handleBlue()); break;
                case "yellow": Platform.runLater(() -> handleYellow()); break;
                case "red": Platform.runLater(() -> handleRed()); break;
                case "enter": Platform.runLater(() -> handleEnter()); break;
            }
        }
    } catch (Exception e) {
        System.out.println("Socket error: " + e.getMessage());
    }
});

socketThread.setDaemon(true);
socketThread.start();



 stage.setScene(rulesScene);
stage.show();
    } 

public void handleEnter(){
        if (currentCol == 4) {
    String[] guess = getGuess();
    checkGuess(guess);
    currentCol = 0;
    
   
    String code = colorCode[0] + ", " + colorCode[1] + ", " + colorCode[2] + ", " + colorCode[3];
    
    if(Arrays.equals(guess,colorCode)) { 
        showMessage("You win!");
        gameOver = true;
    }else{
        currentRow++;
    if (currentRow == 8) {

        showMessage("             You lose!\n       The code was:\n"+ code);
        gameOver = true;
    }
        
       
    } 
    }
} 

    public void handleGreen() {
        if (currentCol < 4){ 
        boxes[currentRow][currentCol].setFill(Color.GREEN);
        playerGuesses[currentRow][currentCol] = "green";
        currentCol++; 

        }


}

public void handleBlue() {
    if (currentCol < 4){ 
        boxes[currentRow][currentCol].setFill(Color.BLUE);
        playerGuesses[currentRow][currentCol] = "blue";
        currentCol++; 
    }
    
}

public void handleYellow() {
    if (currentCol < 4){ 
        boxes[currentRow][currentCol].setFill(Color.YELLOW);
        playerGuesses[currentRow][currentCol] = "yellow";
        currentCol++;
    }
}

public void handleRed(){
    if (currentCol < 4){ 
        boxes[currentRow][currentCol].setFill(Color.RED);
        playerGuesses[currentRow][currentCol] = "red";
        currentCol++; 
    }
    
}



public String[] getGuess(){
    String[] guess = new String[4];
        for(int i=0; i < 4; i++) {
            guess[i] = playerGuesses[currentRow][i];      
        }
        return guess; 
    }
    


  public void checkGuess(String[] guess) { 
    String[] hints = new String[]{"grey", "grey", "grey", "grey"}; 
    boolean[] guessMatched = new boolean[4];
    boolean[] positionMatched = new boolean[4];  
        
    for( int i = 0; i < 4; i++) {
            if (guess[i].equals(colorCode[i])){
                hints[i] = "yellow";
                guessMatched[i] = true; 
                positionMatched[i] = true;
            }
        }

            for( int i = 0; i < 4; i++) {
                for ( int j = 0; j< 4; j++) {
                    if(positionMatched[j] == false && guessMatched[i]== false && guess[i].equals(colorCode[j])){
                        positionMatched[j] = true;
                        guessMatched[i] = true;
                        hints[i] = "red";
                        break;
                    }
                }
                 }
            for(int i = 3; i> 0; i--){
                int j = (int)(Math.random() * (i+1));
                 String temp = hints[i];
                 hints[i] = hints[j];
                 hints[j] = temp;

            }

            for(int i = 0; i < 4; i++){
                if(hints[i].equals("yellow")){
                     hintBoxes[currentRow][i].setFill(Color.YELLOW);
                }else if(hints[i].equals("red")) {
                    hintBoxes[currentRow][i].setFill(Color.RED);
                }else 
                    hintBoxes[currentRow][i].setFill(Color.GREY);

}
if(out != null) {
    String hintMessage = ("hints:" + hints[0] + "," + hints[1] + "," + hints[2] + "," + hints[3]);
    System.out.println("Sending it to Pi:" + hintMessage);
    out.println(hintMessage);
}
    }

        

public void showMessage(String message){
        
   

    final Stage summaryStage = new Stage();
    VBox summaryLayout = new VBox();
    summaryLayout.setSpacing(20);
    summaryLayout.setAlignment(Pos.CENTER);
    Label summaryTitle = new Label(message);
    summaryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");



    summaryLayout.setStyle("-fx-background-color: #121213;");

    Button playAgainBtn = new Button("Play Again");
    playAgainBtn.setOnAction(event -> {
    resetGame();
    summaryStage.close();
});
summaryLayout.getChildren().addAll(summaryTitle, playAgainBtn);
   

    Scene summaryScene = new Scene(summaryLayout, 300, 400);
    summaryStage.setScene(summaryScene);
    summaryStage.show();



}


public String[] getColorCode(){
 for(int i = 0; i < 4; i++) {
    int randomColor= (int)(Math.random() * colors.length);
    colorCode[i] = colors[randomColor];
    
 }
 return colorCode; 
}

public void resetGame(){
    currentRow = 0; 
    currentCol = 0;
    gameOver = false; 
    colorCode= getColorCode();
    playerGuesses = new String[8][4];

    for(int row= 0; row < 8; row++){ 
    for (int col = 0; col < 4; col++) {
        boxes[row][col].setFill(Color.GREY);
    }
}

    for(int row = 0; row < 8; row++){ 
    for(int col = 0; col < 4; col++){
        hintBoxes[row][col].setFill(Color.GREY);
    }
}

}




public static void main(String[] args) {
        launch(args);
    }
}