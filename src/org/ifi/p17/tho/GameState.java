package org.ifi.p17.tho;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.ifi.p17.tho.data.Cell;
import org.ifi.p17.tho.io.GameStateFile;

public class GameState {
	private int state[][];
	private int weight[][];
	public static int BOARD_SIZE = 20;
	public static int NO_PLAYER = 0;
	public static int DRAW = 3;
	public static int X_PLAYER = 2;
	public static int O_PLAYER = 1;
	
	private static int[] SCORES = {0, 1, 10, 100, 1000, 10000};
	
	public int cols = BOARD_SIZE;
	public int rows = BOARD_SIZE;
	private List<Cell> seq = new ArrayList<Cell>();
	private int moveRow = -1;
	private int moveCol = -1;
	public boolean first = false;
	public int winner = 0;
	private int nextPlayer;
	
	public GameState(){
		initEmptyBoard();
		state = GameStateFile.loadStateFromFile();
	}
	
	public GameState(GameState gs){
		initEmptyBoard();
		for(int row = 0; row < rows; row++){
			for(int col = 0; col < cols; col++){
				state[col][row] = gs.at(col, row);
			}			
		}
	}
	
	public void initEmptyBoard(){
		state = new int[BOARD_SIZE][BOARD_SIZE];
		weight = new int[BOARD_SIZE][BOARD_SIZE];
		nextPlayer = NO_PLAYER;
		for(int row = 0; row < rows/2; row++){
			for(int col = 0; col < cols; col++){
				int z = Math.min(row, col);
				weight[row][col] = z + 1;
				weight[rows - row - 1][col] = z + 1;
			}
		}
	}
	
	public boolean endGame(int row, int col) {
		int lastPlayer = state[row][col];
		int y = row;		
		
		int count = 1;
		//row
		int x = col;
		while ( x > 0 && at(row, x - 1) == lastPlayer ){
			count++;
			x--;
		}
		x = col;
		while ( x < cols - 1 && at(row, x + 1) == lastPlayer ){
			count++;
			x++;
		}
		if(count >= Gomoku.WINING_SIZE){
			setWinner(lastPlayer);
			for(int i = 0; i < count; i++){
				//seq[i] = new Cell(row, x - i);
				seq.add(new Cell(row, x - i));
			}
			return true;
		}
		
		//col
		count = 1;
		y = row;
		while ( y > 0 && at(y - 1, col) == lastPlayer ){
			count++;
			y--;
		}
		y = row;
		while ( y < cols - 1 && at(y + 1, col) == lastPlayer ){
			count++;
			y++;
		}
		
		if(count >= Gomoku.WINING_SIZE){
			setWinner(lastPlayer);
			for(int i = 0; i < count; i++){				
				seq.add(new Cell(y - i, col));
			}			
			return true;
		}		
		
		
		//diagonal
		count = 1;
		y = row;
		x = col;		
		while(x > 0 && y > 0 && at(y - 1, x - 1) == lastPlayer){
			count++;
			x--;
			y--;
		}
		y = row;
		x = col;
		while(x < cols - 1 && y < rows - 1 && at(y + 1, x + 1) == lastPlayer){
			count++;
			x++;
			y++;
		}

		if(count >= Gomoku.WINING_SIZE){
			setWinner(lastPlayer);
			for(int i = 0; i < count; i++){				
				seq.add(new Cell(y - i, x - i));
			}						
			return true;
		}
		
		//alternative
		count = 1;
		y = row;
		x = col;		
		while(x > 0 && y < rows - 1 && at(y + 1, x - 1) == lastPlayer){
			count++;
			x--;
			y++;
		}
		y = row;
		x = col;
		while(x < cols - 1 && y > 0 && at(y - 1, x + 1) == lastPlayer){
			count++;
			x++;
			y--;
		}
		
		if(count >= Gomoku.WINING_SIZE){
			setWinner(lastPlayer);
			for(int i = 0; i < count; i++){
				seq.add(new Cell(y + i, x - i));
			}			
			return true;
		}
		return false;
	}
	
	
	public int evaluate() {			
	
		int oValue = 0;
		int xValue = 0;
		
		/*
		 * row -> step 0, 1
		 * col -> step 1, 0
		 * diagonal -> step 1, 1
		 * diagonal alter -> step -1 , 1 
		 */
		for(int i = 0; i < BOARD_SIZE; i++){
			//row i
			oValue += evaluate(i, 0, 0, 1, O_PLAYER);
			xValue += evaluate(i, 0, 0, 1, X_PLAYER);
			//col i
			oValue += evaluate(0, i, 1, 0, O_PLAYER);
			xValue += evaluate(0, i, 1, 0, X_PLAYER);
			//diagonal
			oValue += evaluate(0, i, 1, 1, O_PLAYER);
			xValue += evaluate(0, i, 1, 1, X_PLAYER);
			if( i > 0){
				//don't count main diagonal 2 times
				oValue += evaluate(i, 0, 1, 1, O_PLAYER);
				xValue += evaluate(i, 0, 1, 1, X_PLAYER);
			}
			//diagonal alternative
			oValue += evaluate(BOARD_SIZE - 1, i, -1, -1, O_PLAYER);
			xValue += evaluate(BOARD_SIZE - 1, i, -1, -1, X_PLAYER);			
			if( i < BOARD_SIZE - 1){
				//don't count main diagonal 2 times
				oValue += evaluate(i, BOARD_SIZE - 1, -1, -1, O_PLAYER);
				xValue += evaluate(i, BOARD_SIZE - 1, -1, -1, X_PLAYER);
			}			
		}
		//System.out.println(oValue + "," + xValue);
		//System.out.println(oValue - xValue);
		return oValue - xValue;
	}
	

	/**
	 * diagonal start from (0, col)
	 * coordinates of cells : (0 + i, col + i)
	 * col >= row
	 * @param col
	 * @param player
	 * @return
	 */
	private int evaluate(int startRow, int startCol, int deltaRow, int deltaCol, int player){
		int value = 0;
		//p player sequence		

		int col = startCol;
		int row = startRow;
		int offset = 0;
		
		while(isInsideBoard(row, col)){
			List<Integer> seq = new ArrayList<Integer>();
			while(isInsideBoard(row, col) && at(row, col) == player){
				seq.add(offset);
				offset++;
				row += deltaRow;
				col += deltaCol;
			}
			
			if(seq.size() > 0){				
				int firstRowIndex = startRow + seq.get(0) * deltaRow;
				int firstColIndex = startCol + seq.get(0) * deltaCol;
				
				int lastRowIndex = startRow + seq.get(seq.size() - 1) * deltaRow;
				int lastColIndex = startCol + seq.get(seq.size() - 1) * deltaCol;
				
				//two ways close seq
				/*
				if( (lastRowIndex - startRow < Gomoku.WINING_SIZE && 
					 at(lastRowIndex + deltaRow, lastColIndex + deltaCol) != NO_PLAYER) ||
					 (firstRowIndex > BOARD_SIZE - Gomoku.WINING_SIZE && 
					 at(firstRowIndex + deltaRow, firstColIndex + deltaCol) != NO_PLAYER)|| 
					 (at(firstRowIndex + deltaRow, firstColIndex + deltaCol) != NO_PLAYER && 
					  at(lastRowIndex + deltaRow, lastColIndex + deltaCol) != NO_PLAYER) ){
					continue;
				}
				*/
				int nbCheck = Gomoku.WINING_SIZE - seq.size() + 1;
				
				int leftOpenCount = 0;
				int rightOpenCount = 0;
				int openHeads = 0;
				
				//if( isInsideBoard(lastRowIndex + deltaRow, lastColIndex + deltaCol) &&
				//	at(lastRowIndex + deltaRow, lastColIndex + deltaCol) == NO_PLAYER ){
				for(int i = 0; i < nbCheck; i++){
					int ri = lastRowIndex + i*deltaRow;
					int ci = lastColIndex + i*deltaCol;
					if( !isInsideBoard(ri, ci)){
						break;
					}
					if(at (ri, ci) == NO_PLAYER){
						leftOpenCount++;
					}else{
						break;
					}
				}
				//}

				for(int i = 0; i < nbCheck; i++){
					int ri = firstRowIndex + i*deltaRow;
					int ci = firstColIndex + i*deltaCol;
					if( !isInsideBoard(ri, ci)){
						break;
					}
					if(at (ri, ci) == NO_PLAYER){
						rightOpenCount++;
					}else{
						break;
					}
				}

				if( leftOpenCount > 0){
					openHeads++;
				}
				
				if( rightOpenCount > 0){
					openHeads++;
				}
				
				int openCount = leftOpenCount + rightOpenCount;
				
				if(openHeads == 2){
					//two open head but !! there are only one possibility
					if(openCount + seq.size() == Gomoku.WINING_SIZE){
						value += score(seq.size(), 1);
					}else if(openCount + seq.size() > Gomoku.WINING_SIZE){
						value += score(seq.size(), openHeads);
					}else {//no possibility of winning
						value += score(seq.size(), 0);
					}
				}else{
					value += score(seq.size(), openHeads);
				}
				
			}
			row += deltaRow;
			col += deltaCol;
		}

		return value;	
	}	
	
	
	public boolean endGame2(int row, int col) {		
		int lastPlayer = state[row][col];
		//int dir = 0;
		//int k;
		int count = 1;
		int x = col;
		int y = row;
		//check horizon
		while ( x > 0 && at(x - 1, y) == lastPlayer ){
			x--;
		}
		
		while ( x < cols - 1 && at(x + 1, y) == lastPlayer){
			count++;
			x++;
		}
		
		if (count >= Gomoku.WINING_SIZE){
			setWinner(lastPlayer);
			return true;
		}
		
		// then do the three counts with vertical components
		for (int dx = -1; dx <= 1; dx++) {
			x = col;
			y = row;
			while (y > 0 && x - dx >= 0 && x - dx < cols
					&& at(x - dx, y - 1) == lastPlayer) {
				x -= dx;
				y--;
			}
			count = 1;
			while (y < rows - 1 && x + dx >= 0
					&& x + dx < cols
					&& at(x + dx, y + 1) == lastPlayer) {
				count++;
				x += dx;
				y++;
			}
			
			if (count >= Gomoku.WINING_SIZE){
				setWinner(lastPlayer);
				return true;
			}
		}
		return false;
		
	}
	
	private int score(int numberOfPiece, int open) {

		if (numberOfPiece >= Gomoku.WINING_SIZE) {
			return SCORES[Gomoku.WINING_SIZE];
		}

		if (numberOfPiece == 0 || open == 0) {
			return 0;
		}
		if(open == 2){
			return SCORES[numberOfPiece];
		}
		if(open == 1){
			return SCORES[numberOfPiece - 1]/10;
		}
		return 0;
	}
	
	boolean isInsideBoard(int row, int col){
		return row < BOARD_SIZE && col < BOARD_SIZE && row >= 0 && col >= 0; 
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int row = 0; row < rows; row++){
			for(int col = 0; col < cols; col++){
				sb.append(at(row,col));
				sb.append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public int[][] getState() {
		return state;
	}
	public void setState(int[][] state) {
		this.state = state;
	}	
	public int at(int row, int col){
		if(row < 0 || col < 0 || row > rows -1 || col > cols -1){
			return -1;
		}
		return state[row][col];
	}
	public void set(int row, int col, int player){
		state[row][col] = player;
		setMoveRow(row);
		setMoveCol(col);
	}
	
	public GameState getStateAfterMove(int row, int col, int player){
		GameState gs = new GameState(this);
		gs.set(row, col, player);
		gs.setMoveRow(row);
		gs.setMoveCol(col);
		return gs;
	}
	public boolean getFirst(){
		return first;
	}

	public int getWinner() {
		return winner;
	}

	public void setWinner(int winner) {
		this.winner = winner;
	}

	public int getMoveRow() {
		return moveRow;
	}

	public void setMoveRow(int moveRow) {
		this.moveRow = moveRow;
	}

	public int getMoveCol() {
		return moveCol;
	}

	public void setMoveCol(int moveCol) {
		this.moveCol = moveCol;
	}
	public int getWeight(int row, int col){
		return weight[row][col];
	}

	public int getNextPlayer() {
		return nextPlayer;
	}

	public void setNextPlayer(int nextPlayer) {
		this.nextPlayer = nextPlayer;
	}

	public List<Cell> getWinningSequence() {
		return seq;
	}

	
}
