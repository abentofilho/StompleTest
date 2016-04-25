//package stomple;
package us.ihmc.games.stomple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
//import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import us.ihmc.tools.testing.MutationTestingTools;

public class StompleBoardTest
{
   boolean debugging = false; //true;  //

   //@Ignore
   @Test
   public final void testAnEntireGame() throws Exception
   {
      StompleBoard board = new StompleBoard();
      StompleColor colorBeingMoved = null;

      //set a random number of players 

      int numberOfPlayers = board.randomInRange(2, 6);
      board.setNumberOfStartingPlayers(numberOfPlayers);
      assertTrue(numberOfPlayers <= 6);
      assertTrue(numberOfPlayers >= 2);

      assertEquals(numberOfPlayers, board.getNumberOfStartingPlayers());

      //player list

      board.createPlayerList(numberOfPlayers);
      String playerListTest = board.getPlayerList().toString();
      board.createPlayerList(numberOfPlayers);
      String playerListWillPlay = board.getPlayerList().toString();
      assertTrue(!(playerListTest.equals(playerListWillPlay)));
      //select current player        

      board.resetGame(numberOfPlayers);

      assertTrue(numberOfPlayers >= 2 && numberOfPlayers <= 6);
      assertEquals(board.getPlayerList().size(), numberOfPlayers);
      checkPlayerFrequency(board);

      //set up and show a random board game

      board.setUpARandomBoard();
      String marbleListTest = board.getMarbleList().toString();
      board.setUpARandomBoard();
      String marbleListBoard = board.getMarbleList().toString();
      assertTrue(!(marbleListTest.equals(marbleListBoard)));

      // assert size and frequency marbles of each color on board
      assertTrue(board.getMarbleList().size() == 49);
      checkColorFrequency(board);

      //print player status

      StomplePlayer currentPlayer = board.getCurrentPlayer(board.getIndexOfCurrentPlayer());
      assertTrue(currentPlayer.getLastMove().getLine() == -1);
      assertTrue(currentPlayer.getLastMove().getColumn() == -1);

      //start playing the game

      StomplePosition positionBeingMoved = new StomplePosition(-1, -1);

      System.out.println("*****************Game starts with " + board.getNumberOfPlayers() + " players.****************");

      if (debugging)
         board.printOutBoard();

      outerLoop: while (board.getPlayerList().size() + 1 != 0)
      {
         //while a game is playing 
         while (board.getAPlayerIsPlaying())
         {
            assertTrue(board.getAPlayerIsPlaying());
            //set a first random move for the current player 
            if (currentPlayer.getFirstMoveOfFirstTurn())
            {
               System.out.println("Player: " + currentPlayer.getPlayerName() + " first random move.");
               positionBeingMoved = board.randomPositionOnOuterRow(currentPlayer);
               //               currentPlayer.setLastMove(positionBeingMoved); 
               colorBeingMoved = currentPlayer.getLastMovedColor(); //board.getMarbleColor(positionBeingMoved);
               assertTrue(positionBeingMoved != null);
               assertTrue(!(colorBeingMoved.equals(StompleColor.NONE)));
            }
            //set a next move on same turn to an adjacent position of same color
            else if (currentPlayer.getNextMoveOfSameTurn())
            {
               //can move to an adjacent position of same color if moving in a string
               System.out.println("Player: " + currentPlayer.getPlayerName() + " next move on a string.");
               positionBeingMoved = board.getAdjacentOfSameColor(currentPlayer.getLastMove(), currentPlayer.getLastMovedColor(), currentPlayer);
               currentPlayer.setLastMove(positionBeingMoved);
               colorBeingMoved = currentPlayer.getLastMovedColor();
               assertTrue(positionBeingMoved != null);
               assertTrue(currentPlayer.getLastMovedColor().equals(colorBeingMoved));
               assertTrue(board.checkIfPositionIsOnBoard(positionBeingMoved));
            }
            //set a first move for next turns
            else if (currentPlayer.getFirsMoveOfNextTurn())
            {
               System.out.println("Player: " + currentPlayer.getPlayerName() + " first move of next turns.");
               board.printPlayerStatus(currentPlayer.getLastMove(), currentPlayer.getLastMovedColor());
               positionBeingMoved = board.randomPositionAdjacentTo(currentPlayer.getLastMove(), currentPlayer);
               if (positionBeingMoved == null)
               {
                  positionBeingMoved = board.colorOnBoardSameAsPlayerColor(currentPlayer);
               }

               //player's out if he don't have any move to do

               if (positionBeingMoved == null)
               {
                  //game's over for this player
                  System.out.println("Player: " + currentPlayer.getPlayerName() + " is out!");
                  board.getPlayerList().remove(board.getIndexOfCurrentPlayer());
                  numberOfPlayers = board.getPlayerList().size();
                  board.setNumberOfPlayers(numberOfPlayers);
                  board.setAPlayerIsPlaying(false);
                  if (numberOfPlayers == 1)
                     break outerLoop;
                  break;
               }
               else
               {
                  currentPlayer.setLastMove(positionBeingMoved);
                  colorBeingMoved = currentPlayer.getLastMovedColor();
                  assertTrue(positionBeingMoved != null);
                  assertTrue(!(currentPlayer.getLastMovedColor().equals(board.getMarbleColor(positionBeingMoved))));
                  assertTrue(board.checkIfPositionIsOnBoard(positionBeingMoved));
               }

            }

            //check for adjacent marble of same color

            boolean positionBeingMovedHasAdjacentColor = board.checkIfAnyAdjacentPieceHasColor(positionBeingMoved, colorBeingMoved); //  (yet for one dimension marble list - TBR)

            //   if this is the first move of current player

            if (currentPlayer.getFirstMoveOfFirstTurn())
            {
               //must move marble on marble boards and which color isn't NONE 

               if (board.checkIfPositionIsOnOuterRow(positionBeingMoved) && (colorBeingMoved != StompleColor.NONE))
               {

                  //reset first turn of current player

                  currentPlayer.setFirstMoveOfFirstTurn(false);

                  //Process moving: 

                  board.processStomple(currentPlayer, positionBeingMoved, colorBeingMoved);

                  if (positionBeingMovedHasAdjacentColor)
                  {
                     currentPlayer.setNextMoveOfSameTurn(true);
                     continue;
                  }
                  //else if there isn't adjacent colors to the position being moved
                  else
                  {
                     //set turn of next game for this player

                     currentPlayer.setFirstMoveOfNextTurn(true);

                     //select the next player

                     board.setAPlayerIsPlaying(false);
                  }
               }
               else //not first move of this player is on borders
               {
                  //
                  //while not the position being moved isn't on the borders 
                  //    reject moved position and throw error message        
                  //    allow another move for current player

                  System.out.println("First turn must move on outer row.");
               }
            }
            //this is the second or following moves of a current player turn

            else if (currentPlayer.getNextMoveOfSameTurn())
            {

               board.processStomple(currentPlayer, positionBeingMoved, colorBeingMoved);

               //verify if there is(are) adjacent color(s)  to allow(or not allow) another move for the player

               if (positionBeingMovedHasAdjacentColor)
               {
                  continue;
               }
               //else if there isn't adjacent colors to the position being moved
               else
               {
                  //allow the selection of the next player

                  board.setAPlayerIsPlaying(false);

                  //reset nextMoveOfSameTurn

                  currentPlayer.setNextMoveOfSameTurn(false);

                  //set next move of next turn 

                  currentPlayer.setFirstMoveOfNextTurn(true);
               }

            }
            //this is the first move of next player's turns

            else if (currentPlayer.getFirsMoveOfNextTurn())
            {
               /*
                * player can move to a position adjacent to last move done or
                * can move to a marble of same player's color on the outer row
                */

               assertTrue(currentPlayer.getFirsMoveOfNextTurn());
               board.processStomple(currentPlayer, positionBeingMoved, colorBeingMoved);

               //reset reset firsMoveOfNextTurn

               currentPlayer.setFirstMoveOfNextTurn(false);
               assertTrue(!(currentPlayer.getFirsMoveOfNextTurn()));
               assertFalse(currentPlayer.getFirsMoveOfNextTurn());

               //set nextMoveOfSameTurn

               //               currentPlayer.setNextMoveOfSameTurn(true);

               //verify if there is(are) adjacent color(s)  to allow(or not allow) another move for the player

               if (positionBeingMovedHasAdjacentColor)
               {
                  assertTrue(positionBeingMovedHasAdjacentColor);
                  currentPlayer.setNextMoveOfSameTurn(true);
                  assertTrue(currentPlayer.getNextMoveOfSameTurn());
                  continue;
               }
               //else if there isn't adjacent colors to the position being moved
               else
               {
                  //allow the selection of the next player

                  assertTrue(board.getAPlayerIsPlaying());
                  board.setAPlayerIsPlaying(false);

                  //set 

                  //set next move of next turn 

                  assertTrue(board.getCurrentPlayer(board.getIndexOfCurrentPlayer()) == currentPlayer);
                  currentPlayer.setFirstMoveOfNextTurn(true);
               }

            }

         }
         //increments indexOfCurrentPlayer

         int indexOfCurrentPlayer = board.getIndexOfCurrentPlayer() + 1;

         //verify to restart the line

         indexOfCurrentPlayer = indexOfCurrentPlayer <= board.getNumberOfPlayers() - 1 ? indexOfCurrentPlayer : 0;
         assertTrue(indexOfCurrentPlayer >= 0 && indexOfCurrentPlayer <= 6);

         //set new index 

         board.setIndexOfCurrentPlayer(indexOfCurrentPlayer);
         assertTrue(board.getIndexOfCurrentPlayer() == indexOfCurrentPlayer);
         //select new player

         currentPlayer = board.getCurrentPlayer(board.getIndexOfCurrentPlayer());
         assertTrue(currentPlayer != null);

         //set getAPlayerIsPlaying

         board.setAPlayerIsPlaying(true);
         assertTrue(board.getAPlayerIsPlaying());
      }
      System.out.println("***********************Game's over!*************************");
      int points = 3;
      if (debugging)
         board.printOutBoard();
      for (StompleColor color : StompleColor.values())
      {
         assert (color != null);
         if (debugging)
            System.out.println("Index of color:" + board.getMarbleList().indexOf(color) + " Color: " + color.toString());
         assert (board.getMarbleList() != null);
         if (board.getMarbleList().indexOf(color) >= 0)
            points = board.getMarbleList().get(board.getMarbleList().indexOf(color)).equals(StompleColor.NONE) ? points
                  : points + Collections.frequency(board.getMarbleList(), color);
      }
      int bonusPoints = Collections.frequency(board.getMarbleList(), StompleColor.WHITE);
      points = points + 2 * bonusPoints;
      System.out.println("The winner of this round with " + board.getNumberOfStartingPlayers() + " players is " + board.getPlayerList().get(0).getPlayerName()
            + " \n\t\t\t with " + points + " points.");
      System.out.println("************************************************************");
      //   
   }

   @Ignore
   @Test

   public final void TestCheckAdjacentPositionDirectionIsOnBoard()
   {
      /**
       * Returns true if the adjacent piece to the StomplePosition position in the StompleDirection direction is on board.
       * @param position StomplePosition of the center piece to check the adjacent piece in StompleDirection direction.
       * @param direction StompleDirection of the adjacent piece being checked to be on board
       * @return
       */
      StompleBoard board = new StompleBoard();
      System.out.println("---------TestCheckAdjacentPositionDirectionIsOnBoard starts.--");
      board.setUpARandomBoard();
      int position = 0;
      for (StompleDirection direction : StompleDirection.values())
      {
         if(debugging)
            System.out.print(position + " ");
         StomplePosition newPosition = new StomplePosition(board.randomInRange(0, 6), board.randomInRange(0, 6));
         boolean adjacentOnBoard = board.checkAdjacentPositionDirectionIsOnBoard(newPosition, direction);
         if (adjacentOnBoard)
            assertTrue(adjacentOnBoard);
         else
            if(debugging)
               System.out.println("No adjacent position on board.");
         position++;
      }                             
      System.out.println("---------TestCheckAdjacentPositionDirectionIsOnBoard ends.----");
   }

   @Ignore
   @Test

   public final void TestGetAdjacentOfSameColor_OnOuterRow()
   {
      /**
       * Returns StomplePosition adjacentOfSameColor adjacent of same color to StomplePosition moveYetDone.
       * @param moveYetDone StomplePosition is the position of the piece from which to check to be adjacent.
       * @return
       */
      StompleBoard board = new StompleBoard();
      boolean debugging = false; //true;  //
      
      board.setUpARandomBoard();
      board.resetGame(board.randomInRange(2, 6));
      List<StompleColor> marbleList = board.getMarbleList();

      //test adjacent of same color from marbles not on outer row
      System.out.println("---------Test getAdjacentOfSameColor_OnOuterRow starts.-------");
      if(debugging)
         board.printOutBoard();

      do
      {
         StompleColor colorYetMoved = null;

         int outerRow = board.randomInRange(0, 3);
         int line = 0;
         int column = 0;
         switch (outerRow)
         {
         case 0:
         {
            line = 0;
            column = board.randomInRange(0, 6);
            break;
         }
         case 1:
         {
            line = board.randomInRange(0, 6);
            column = 6;
            break;
         }
         case 2:
         {
            line = 6;
            column = board.randomInRange(0, 6);
            break;
         }
         case 3:
         {
            line = board.randomInRange(0, 6);
            column = 0;
            break;
         }
         default:
         {
            throw new RuntimeException();
         }
         }
         StomplePosition moveYetDone = new StomplePosition(line, column);

         if (moveYetDone != null)
         {
            assertTrue(moveYetDone != null);

            if (board.checkIfPositionIsOnBoard(moveYetDone))
            {
               assertTrue(board.checkIfPositionIsOnBoard(moveYetDone));

               if (!(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE)))
               {
                  assertTrue(!(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE)));
                  colorYetMoved = board.getMarbleColor(moveYetDone);
               }
               else
               {
                  if(debugging)
                     System.out.println("Random position (" + moveYetDone.getLine() + "," + moveYetDone.getColumn() + ") to get adjacent is empty.");
                  continue;
               }
            }
            else
            {
               if(debugging)
                  System.out.println("Random position (" + moveYetDone.getLine() + "," + moveYetDone.getColumn() + ") to get adjacent not on board.");
               continue;
            }
         }
         if(debugging)
            System.out.print("Last moved: (" + moveYetDone.getLine() + "," + moveYetDone.getColumn() + ") Color: " + colorYetMoved.toString() + " ");
         //this is the method being tested
         StomplePlayer player = new StomplePlayer();
         StomplePosition adjacentOfSameColor = board.getAdjacentOfSameColor(moveYetDone, colorYetMoved, player);
         if (adjacentOfSameColor != null)
         {
            board.getMarbleList().remove(moveYetDone.getLine() * 7 + moveYetDone.getColumn());
            board.getMarbleList().add(moveYetDone.getLine() * 7 + moveYetDone.getColumn(), StompleColor.NONE);
            assertTrue(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE));
            board.getMarbleList().remove(adjacentOfSameColor.getLine() * 7 + adjacentOfSameColor.getColumn());
            board.getMarbleList().add(adjacentOfSameColor.getLine() * 7 + adjacentOfSameColor.getColumn(), StompleColor.NONE);
            assertTrue(board.getMarbleColor(adjacentOfSameColor).equals(StompleColor.NONE));
            if(debugging)
               System.out.println("Adjacent of same color: (" + adjacentOfSameColor.getLine() + "," + adjacentOfSameColor.getColumn() + ")");
         }
         else
         {
            board.getMarbleList().remove(moveYetDone.getLine() * 7 + moveYetDone.getColumn());
            board.getMarbleList().add(moveYetDone.getLine() * 7 + moveYetDone.getColumn(), StompleColor.NONE);
            assertTrue(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE));
            if(debugging)
               System.out.println("Not an adjacent of same color.");
         }
         if(debugging){
            board.printOutBoard();
            if(debugging)
               System.out.println("------------------New turn-----------------------");
         }
      }
      while (Collections.frequency(marbleList, StompleColor.NONE) < 20);// marbleList.size());
      System.out.println("---------Test getAdjacentOfSameColor_OnOuterRow ends.---------");
   }

   @Ignore
   @Test
   public final void TestGetAdjacentOfSameColor() //_NotOnOuterRow
   {
      /**
       * Returns StomplePosition adjacentOfSameColor to StomplePosition moveYetDone.
       * @param moveYetDone StomplePosition is the position of the piece from which to check to be adjacent.
       * @return
       */
      StompleBoard board = new StompleBoard();
      boolean debugging = false; //true;  //
      StomplePlayer anyPlayer = new StomplePlayer();
      board.setUpARandomBoard();
      board.resetGame(board.randomInRange(2, 6));
      List<StompleColor> marbleList = board.getMarbleList();

      //test adjacent of same color from marbles not on outer row
      System.out.println("---------Test getAdjacentOfSameColor starts-------------------"); //_NotOnOuterRow starts.
      if (debugging)
         board.printOutBoard();

      do
      {
         StompleColor colorYetMoved = null;
         StomplePosition moveYetDone = new StomplePosition(board.randomInRange(0, 6), board.randomInRange(0, 6));
         if (moveYetDone != null)
         {
            assertTrue(moveYetDone != null);

            if (board.checkIfPositionIsOnBoard(moveYetDone))
            {
               assertTrue(board.checkIfPositionIsOnBoard(moveYetDone));

               if (!(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE)))
               {
                  assertTrue(!(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE)));
                  colorYetMoved = board.getMarbleColor(moveYetDone);
                  //continue;
               }
               else
               {
                  if(debugging)
                     System.out.println("Random position (" + moveYetDone.getLine() + "," + moveYetDone.getColumn() + ") to get adjacent is empty.");
                  continue;
               }
            }
            else
            {
               if(debugging)
                  System.out.println("Random position (" + moveYetDone.getLine() + "," + moveYetDone.getColumn() + ") to get adjacent not on board.");
               continue;
            }
         }
         if(debugging)
            System.out.print("Last moved: (" + moveYetDone.getLine() + "," + moveYetDone.getColumn() + ") Color: " + colorYetMoved.toString() + " ");
         //this is the method being tested
         StomplePosition adjacentOfSameColor = board.getAdjacentOfSameColor(moveYetDone, colorYetMoved, anyPlayer);
         if (adjacentOfSameColor != null)
         {
            board.getMarbleList().remove(moveYetDone.getLine() * 7 + moveYetDone.getColumn());
            board.getMarbleList().add(moveYetDone.getLine() * 7 + moveYetDone.getColumn(), StompleColor.NONE);
            assertTrue(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE));
            board.getMarbleList().remove(adjacentOfSameColor.getLine() * 7 + adjacentOfSameColor.getColumn());
            board.getMarbleList().add(adjacentOfSameColor.getLine() * 7 + adjacentOfSameColor.getColumn(), StompleColor.NONE);
            assertTrue(board.getMarbleColor(adjacentOfSameColor).equals(StompleColor.NONE));
            if (debugging)
               System.out.println("Adjacent of same color: (" + adjacentOfSameColor.getLine() + "," + adjacentOfSameColor.getColumn() + ")");
         }
         else
         {
            board.getMarbleList().remove(moveYetDone.getLine() * 7 + moveYetDone.getColumn());
            board.getMarbleList().add(moveYetDone.getLine() * 7 + moveYetDone.getColumn(), StompleColor.NONE);
            assertTrue(board.getMarbleColor(moveYetDone).equals(StompleColor.NONE));
            if (debugging)
               System.out.println("Not an adjacent of same color.");
         }
         if (debugging)
         {
            board.printOutBoard();
            System.out.println("------------------New turn-----------------------");
         }
      }
      while (Collections.frequency(marbleList, StompleColor.NONE) < 30);//marbleList.size()); // 

      System.out.println("---------Test getAdjacentOfSameColor ends.--------------------");

   }

   @Ignore
   @Test
   public final void testRandomPositionAdjacentTo()
   {
      /**
       * Returns a random StomplePosition randomPosition adjacent to StomplePosition position.
       * @param position StomplePosition of the piece to find randomly an adjacent position.
       * @return
       */
      StompleBoard board = new StompleBoard();
      boolean aGameIsBeingPlayed = true;
      boolean debugging = false; //true;  //
      StompleColor colorBeingMoved = null;
      
      board.setUpARandomBoard();
      board.resetGame(board.randomInRange(2, 6));
      List<StompleColor> marbleList = board.getMarbleList();

      //test random adjacent of same color from marbles not on board

      board.setUpARandomBoard();
      System.out.println("---------Test randomPositionAdjacentTo starts.----------------");
     
      do
      {
         //Put some NONE colors randomly on side of marblePosition
         StomplePosition anyPosition = new StomplePosition(board.randomInRange(0, 6), board.randomInRange(0, 6));
         if (board.checkIfPositionIsOnBoard(anyPosition))
         {
            if (!(board.getMarbleColor(anyPosition).equals(StompleColor.NONE)))
            {
               if(debugging)
                  System.out.println("Random position (" + anyPosition.getLine() + "," + anyPosition.getColumn() + ") OK.");
               board.getMarbleList().remove(anyPosition.getLine() * 7 + anyPosition.getColumn());
               board.getMarbleList().add(anyPosition.getLine() * 7 + anyPosition.getColumn(), StompleColor.NONE);
            }
            else
            {
               if(debugging)
                  System.out.println("Random position (" + anyPosition.getLine() + "," + anyPosition.getColumn() + ") to get adjacent is empty.");
               continue;
            }
         }
         assertTrue(board.checkIfPositionIsOnBoard(anyPosition));
         assertTrue((board.getMarbleColor(anyPosition).equals(StompleColor.NONE)));
         assertTrue(anyPosition != null);
         assertTrue(anyPosition.getLine() >= 0 && anyPosition.getLine() < 7);
         assertTrue(anyPosition.getColumn() >= 0 && anyPosition.getColumn() < 7);
 
         //this is the method being tested

         StomplePosition randomAdjacentTo = board.randomPositionAdjacentTo(anyPosition, null);
         assert (randomAdjacentTo != null);
         if (debugging)
         {
            board.printOutBoard();
            if (Collections.frequency(marbleList, StompleColor.NONE) < marbleList.size())
            {
               System.out.println("------------------New turn-----------------------");
            }
         }
      }
      while (Collections.frequency(marbleList, StompleColor.NONE) < 7);//marbleList.size());
      System.out.println("---------Test randomPositionAdjacentTo ends.------------------");
   }

   @Ignore
   @Test
   public final void testCheckIfPositionsAreAdjacent()
   {
      /**
       * Returns true if the StomplePosition moveToDo is adjacent StomplePosition moveYetDone.
       * @param moveToDo StomplePosition of the piece to check if adjacent to StomplePosition moveYetDone.
       * @param moveYetDone StomplePosition is the position of the piece from which to check to be adjacent.
       * @return
       */
      StompleBoard board = new StompleBoard();
      boolean debugging = false; //true;  //
      
      //   public boolean checkIfPositionsAreAdjacent(StomplePosition moveToDo, StomplePosition moveYetDone)
      System.out.println("---------Test checkIfPositionsAreAdjacent starts.-------------");
      board.setUpARandomBoard();
      String playerBoardTest = board.getPlayerList().toString();
      board.setUpARandomBoard();
      String playerBoard = board.getPlayerList().toString();
      assertTrue((playerBoardTest.equals(playerBoard)));
      board.resetGame(board.randomInRange(2, 6));

      //test random adjacent of same color from marbles not on board

      board.setUpARandomBoard();
      if(debugging)
         board.printOutBoard();
      int counter = 0;
      do
      {
         StomplePosition moveYetDone = new StomplePosition(board.randomInRange(0, 6), board.randomInRange(0, 6));
         assertTrue(moveYetDone != null);

         //check if adjacent position is on board  

         for (StompleDirection direction : StompleDirection.values())
         {
            if (board.checkAdjacentPositionDirectionIsOnBoard(moveYetDone, direction))
            {
               assertTrue(board.checkAdjacentPositionDirectionIsOnBoard(moveYetDone, direction));
               StomplePosition moveToDo = moveYetDone.getAdjacentPosition(direction);
               assertTrue(board.checkIfPositionIsOnBoard(moveYetDone));
               assertTrue(board.checkIfPositionsAreAdjacent(moveToDo, moveYetDone));
            }
         }
         counter++;
      }
      while (counter < board.getMarbleList().size());
      System.out.println("---------Test checkIfPositionsAreAdjacent ends.---------------");
   }
   
   @Ignore
   @Test
   public final void testCreateBoard()
   {
      System.out.println("---------Test testCreateBoard starts.-------------------------");
      String[] boardString = new String[] {"M  Y  B  G  Y  G  W", "G  B  R  Y  Y  R  R", "O  R  G  B  G  O  R", "O  O  Y  R  W  B  M", "W  Y  M  G  B  Y  M",
            "W  R  R  G  M  M  Y", "R  Y  M  R  B  G  O"};
      StompleBoard board = new StompleBoard(boardString);
      //player list
      int numberOfPlayers = board.randomInRange(2, 6);
      board.setNumberOfStartingPlayers(numberOfPlayers);
      assertTrue(numberOfPlayers <= 6);
      assertTrue(numberOfPlayers >= 2);
      board.createPlayerList(numberOfPlayers);
      String playerListTest = board.getPlayerList().toString();
      board.createPlayerList(numberOfPlayers);
      String playerListWillPlay = board.getPlayerList().toString();
      assertTrue(!(playerListTest.equals(playerListWillPlay)));
      if (debugging)
         board.printOutBoard();
      assertEquals(StompleColor.MAGENTA, board.getColor(0, 0));
      assertEquals(StompleColor.ORANGE, board.getColor(2, 5));
      assertEquals(StompleColor.BLACK, board.getColor(0, 2));
      StomplePlayer currentPlayer = board.getCurrentPlayer(board.getIndexOfCurrentPlayer());
      assertTrue(board.getPlayerList().contains(currentPlayer));
      board.makeAMoveOld(0, 0);
      if(debugging)
         board.printOutBoard();
      assertEquals(StompleColor.NONE, board.getColor(0, 0));
      StomplePlayer nextPlayer = board.getCurrentPlayer(board.getIndexOfCurrentPlayer());
      assertTrue(board.getPlayerList().contains(nextPlayer));
      do
      {
         int anotherPlayer = board.randomInRange(0, numberOfPlayers-1);
         nextPlayer = board.getPlayerList().get(anotherPlayer);
         assertTrue(board.getPlayerList().contains(nextPlayer));
      }while (currentPlayer.equals(nextPlayer));
      assertFalse(nextPlayer == currentPlayer);
      board.makeAMoveOld(5, 0);
      if (debugging)
         System.out.println("board.MakeAMove() OK" + "\n");
      assertEquals(StompleColor.NONE, board.getColor(5, 0));
      System.out.println("---------Test testCreateBoard ends.---------------------------");

   }

   @Ignore
   @Test
   public final void testCheckIfAnyAdjacentPieceHasColor()
   {
      System.out.println("---------Test testCheckIfAnyAdjacentPieceHasColor starts.-----");
      String[] boardString = new String[] {"M  Y  B  G  Y  G  W", "G  B  R  Y  Y  R  R", "O  R  .  B  G  O  R", "O  O  Y  R  W  B  M", "W  Y  M  G  B  Y  M",
            "W  R  R  G  M  M  Y", "R  Y  M  R  B  G  O"};

      StompleBoard boardTest = new StompleBoard(boardString);
      StompleBoard board= new StompleBoard(boardString);
      assertTrue(!(boardTest.equals(board)));

      StomplePosition position = new StomplePosition(0, 0);
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.YELLOW));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.GREEN));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.BLACK));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.MAGENTA));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.ORANGE));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.RED));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.WHITE));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.NONE));

      position = new StomplePosition(2, 1);
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.ORANGE));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.GREEN));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.NONE));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.YELLOW));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.BLACK));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.MAGENTA));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.RED));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.WHITE));

      position = new StomplePosition(6, 6);
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.GREEN));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.YELLOW));
      assertTrue(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.MAGENTA));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.ORANGE));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.NONE));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.BLACK));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.RED));
      assertFalse(board.checkIfAnyAdjacentPieceHasColor(position, StompleColor.WHITE));
      
      System.out.println("---------Test testCheckIfAnyAdjacentPieceHasColor ends.-------");

   }

   @Ignore
   @Test
   public final void testBuildRandomString()
   {
      System.out.println("---------Test buildRandomString starts.----------------------");
      StompleBoard boardTest = new StompleBoard();
      StompleBoard board = new StompleBoard();
      assertTrue(!(boardTest.equals(board)));
      board.buildRandomString();
      String randomStringOne = board.buildRandomString().toString();
      board.buildRandomString();
      String randomStringTwo = board.buildRandomString().toString();
      board.buildRandomString();
      String randomStringThree = board.buildRandomString().toString();
      assertTrue(!(randomStringOne.equals(randomStringThree)));
      assertTrue(!(randomStringTwo.equals(randomStringThree)));
      assertTrue(!(randomStringOne.equals(randomStringThree)));
      System.out.println("---------Test buildRandomString ends.-----------------------");
      
   }
   
   @Ignore
   @Test
   public final void TestCreatePlayerList()
   {
      System.out.println("---------Test createPlayerList starts.----------------------");
      StompleBoard boardTest = new StompleBoard();
      StompleBoard board = new StompleBoard();
      assertTrue(!(boardTest.equals(board)));

      //player list and intrude players to start

      int players = board.randomInRange(2, 6);
      assertTrue(board.getPlayerList().isEmpty());
      board.createPlayerList(players);
      assertTrue(!(board.getPlayerList().isEmpty()));

      int intrudeOne = board.randomInRange(1, players - 1);
      int intrudeTwo = 0;
      do
      {
         intrudeTwo = board.randomInRange(1, players - 1);
      }
      while (intrudeTwo == intrudeOne);
      int intrudeThree = 0;
      do
      {
         intrudeThree = board.randomInRange(1, players - 1);
      }
      while (intrudeThree == intrudeOne || intrudeThree == intrudeTwo);

      String playersListOne = null;
      String playersListTwo = null;
      String playersListThree = null;

      StomplePlayer intrudePlayerOne = null;
      StomplePlayer intrudePlayerTwo = null;
      StomplePlayer intrudePlayerThree = null;

      intrudePlayerOne = board.getPlayerList().get(intrudeOne);
      if (players > 2)
         intrudePlayerTwo = board.getPlayerList().get(intrudeTwo);
      if (players > 4)
         intrudePlayerThree = board.getPlayerList().get(intrudeThree);

      //first player list with one intruder to test

      board.createPlayerList(players);
      board.getPlayerList().add(intrudePlayerOne);
      playersListOne = board.getPlayerList().toString();
      board.getPlayerList().clear();

      //second player list with another intruder to test if only 2 players

      if (players == 2)
      {
         board.createPlayerList(players);
         board.getPlayerList().add(intrudePlayerOne);
         playersListTwo = board.getPlayerList().toString();
         board.getPlayerList().clear();
         assertTrue((playersListOne.equals(playersListTwo)));
      }

      //second player list with another intruder to test if more than 2 players

      else if (players ==3)
      {
         board.createPlayerList(players);
         board.getPlayerList().add(intrudePlayerTwo);
         playersListTwo = board.getPlayerList().toString();
         board.getPlayerList().clear();
         assertTrue(!(playersListOne.equals(playersListTwo)));
      }
      //third player list to test if more than 4 players

      else if (players > 3)
      {
         board.createPlayerList(players);
         board.getPlayerList().add(intrudePlayerTwo);
         playersListTwo = board.getPlayerList().toString();
         board.getPlayerList().clear();
         assertTrue(!(playersListOne.equals(playersListTwo)));

         board.createPlayerList(players);
         board.getPlayerList().add(intrudePlayerThree);
         playersListThree = board.getPlayerList().toString();
         
         assertTrue(!(playersListOne.equals(playersListThree)));
         assertTrue(!(playersListTwo.equals(playersListThree)));
         assertTrue(!(playersListOne.equals(playersListTwo)));     
      }
      System.out.println("---------Test createPlayerList ends.------------------------");
   }
   
   
   
   
// test methods   
   
   public void checkColorFrequency(StompleBoard board)
   {
      for (StompleColor iColor : StompleColor.values())
      {
         if (iColor != StompleColor.NONE)
         {
            List<StompleColor> marbleList = board.getMarbleList();
            int freqOfColor = Collections.frequency(marbleList, iColor);

            if (board.getDebugging())
               System.out.println("Frequency of " + iColor + " is " + freqOfColor);
            assertEquals(freqOfColor, StompleColor.values().length - 1);
         }
      }
   }

   public void checkPlayerFrequency(StompleBoard board)
   {
      List<StomplePlayer> playerList = board.getPlayerList();
      for (int playerIndex = 0; playerIndex < board.getPlayerList().size() - 1; playerIndex++)
      {
         int freqOfPlayer = Collections.frequency(playerList, board.getPlayerList().get(playerIndex));
         assertEquals(freqOfPlayer, 1);
         if (board.getDebugging())
            System.out.println("Frequency of " + playerIndex + " is " + freqOfPlayer);
      }
   }
   
   public static void main(String[] args) throws URISyntaxException, IOException
   {
      String targetTests = "us.ihmc.games.stomple.StompleBoardTest";
      String targetClasses = "us.ihmc.games.stomple.StompleBoard";
      MutationTestingTools.doPITMutationTestAndOpenResult(targetTests, targetClasses);
   }
}
