package org.ifi.p17.tho;

import java.util.ArrayList;
import java.util.List;

import org.ifi.p17.tho.data.Cell;
import org.ifi.p17.tho.data.MoveValue;

public class AIPlayer extends Player {

	private static int[] SCORES = {0, 2, 10, 50, 500, 10000, 1000000};
	private GameState gameState;
	private Cell lastMove;
	
	
	int bestRow = -1;
	int bestCol = -1;
	private Cell bestMove = new Cell(-1, -1);

	public AIPlayer(int id) {
		super(id);
	}
	public void setGameState(GameState gs) {

		//gameState = new GameState(gs);
		//bestMove = new MoveValue(Integer.MIN_VALUE, -1, -1);
		gameState = gs;
	}

	public Cell getBestMove() {
		// logger.debug("begin search
		// ---------------------------------------------------------");
		// logger.debug("Initiale state:");
		// logger.debug("\n" + board.toString());
		int vl;
		if (getId() == gameState.O_PLAYER) {
			vl = max(gameState, Integer.MIN_VALUE, Integer.MAX_VALUE, Gomoku.MAX_DEPTH);
		} else {
			vl = min(gameState, Integer.MIN_VALUE, Integer.MAX_VALUE, Gomoku.MAX_DEPTH);
		}
		System.out.println(bestMove.toString() + "value:" + vl);
		return bestMove;
	}

	private int max(GameState gs, int alpha, int beta, int depth) {
		if (depth == 0) {
			return gs.evaluate();
		}
		
		int deb[][] = new int[20][20];
		for(int i = 0; i < 20; i++){
			for(int j =0; j < 20; j++){
				int val = gs.at(i, j);
				deb[i][j] = val;
			}
		}
		List<Cell> nextMoves = generateMoves(gs);
		System.out.println(gs.toString());
		for (Cell move : nextMoves) {
			GameState newGs = new GameState(gs);
			newGs.set(move.getY(), move.getX(), newGs.getNextPlayer());
			int value = min(newGs, alpha, beta, depth - 1);
			deb[move.getY()][move.getX()] = value;
			if (depth == Gomoku.MAX_DEPTH) {				
				System.out.println("move: " + move.getY() + "," + move.getX() + "," + "vl:" + value);
				
			}

//System.out.println(value + "," + alpha);
			if (value > alpha) {
				alpha = value;
				if (getId() == gameState.O_PLAYER && depth == Gomoku.MAX_DEPTH) {
					bestMove = move;
					System.out.println("alpha:" + alpha);
				}
			}

			if (alpha >= beta) {
				return beta;
			}
		}
		StringBuffer sb = new StringBuffer();
		for(int row = 0; row < 20; row++){
			for(int col = 0; col < 20; col++){
				sb.append(deb[row][col]);
				sb.append(" ");
			}
			sb.append("\n");
		}
		System.out.println(sb.toString());		
		return alpha;
	}

	private int min(GameState gs, int alpha, int beta, int depth) {
		if (depth == 0) {
			return gs.evaluate();
		}

		List<Cell> nextMoves = generateMoves(gs);
		for (Cell move : nextMoves) {
			GameState newGs = new GameState(gs);
			newGs.set(move.getY(), move.getX(), newGs.getNextPlayer());						
			int value = max(newGs, alpha, beta, depth - 1);
			
			if (value < beta) {
				beta = value;
				if (getId() == gameState.X_PLAYER && depth == Gomoku.MAX_DEPTH) {
					bestMove = move;
					System.out.println("beta:" + beta);
				}
			}
			// board
			if (alpha >= beta) {
				return alpha;
			}
		}
		return beta;
	}	
	
	private List<Cell> generateMoves(GameState gameState) {
		List<Cell> nextMoves = new ArrayList<Cell>();
		
//		if(gameState.getWinner() > 0){
//			return nextMoves;
//		}
		
		int[] bounds = getBounds(gameState);
		
		// check endGame Here
		for (int row = bounds[0]; row < bounds[2]; row++) {
			for (int col = bounds[1]; col < bounds[3]; col++) {
				if (gameState.at(row, col) == GameState.NO_PLAYER) {
					nextMoves.add(new Cell(row, col));
				}
			}
		}
		return nextMoves;
	}
	private int[] getBounds(GameState gs){
		//return new int[] {0, 0, gs.rows, gs.cols};
		
		int[] lower = getLowerBounds(gs);
		int[] upper = getUpperBounds(gs);
		int lowerRow = lower[0];
		int lowerCol = lower[1];
		
		int upperRow = upper[0];
		int upperCol = upper[1];
//System.out.println("bounds" + lowerRow + "," + lowerCol + "," +  upperRow + "," + upperCol);		
		lowerRow = (lowerRow > 2)?lowerRow - 2: 0;
		lowerCol = (lowerCol > 2)?lowerCol - 2: 0;
		
		upperRow = (upperRow < gs.rows - 2)? upperRow + 2: gs.rows;
		upperCol = (upperCol < gs.cols - 2)? upperCol + 2: gs.cols;
//System.out.println("bounds" + lowerRow + "," + lowerCol + "," +  upperRow + "," + upperCol);		
		return new int[] {lowerRow, lowerCol, upperRow, upperCol};	
	}
	
	private int[] getLowerBounds(GameState gs){
		int lowerCol = 0; 
		int lowerRow = 0;
		outter:
		for (int row = 0; row < gs.rows/2; row++) {
			for (int col = 0; col < gs.cols; col++) {
				lowerCol = col;
				lowerRow = row;	
				if(gs.at(row, col) > 0){
					break outter;
					//return new int[] {lowerRow, lowerCol};
				}
			}
		}
		outter2:
		for (int col = 0; col < gs.cols/2; col++) {
			for (int row = 0; row < gs.rows; row++) {
				if(gs.at(row, col) > 0){
					lowerRow = Math.min(lowerRow, row);
					lowerCol = Math.min(lowerCol, col);
					break outter2;
					//return new int[] {lowerRow, lowerCol};
				}				
			}
		}
		lowerRow = (lowerRow > gs.rows/2) ? gs.rows/2 : lowerRow;
		lowerCol = (lowerCol > gs.cols/2) ? gs.cols/2 : lowerCol;

		return new int[] {lowerRow, lowerCol};
	}	
	
	private int[] getUpperBounds(GameState gs){		
		int upperCol = 0; 
		int upperRow = 0;
		
		outter:
		for (int row = gs.rows - 1; row > gs.rows/2; row--) {
			for (int col = gs.cols - 1; col > 0; col--) {
				upperCol = col;
				upperRow = row;
				if(gs.at(row, col) > 0){
					break outter;
					//return new int[] {upperRow, upperCol};
				}
			}
		}
		
		outter2:
		for (int col = gs.cols - 1; col > gs.rows/2; col--) {
			for (int row = gs.rows - 1; row > 0; row--) {			
				if(gs.at(row, col) > 0){
					upperRow = Math.max(upperRow, row);
					upperCol = Math.max(upperCol, col);
					break outter2;
					//return new int[] {upperRow, upperCol};
				}
			}
		}
		upperRow = (upperRow < gs.rows/2) ? gs.rows/2 : upperRow;
		upperCol = (upperCol < gs.cols/2) ? gs.cols/2 : upperCol;
		
		return new int[] {upperRow, upperCol};
	}	
}
