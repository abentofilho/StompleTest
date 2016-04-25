package us.ihmc.games.stomple;
//package stomple;

import static org.junit.Assert.*;

import java.util.ArrayList;

//package us.ihmc.games.stomple;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class StompleTest
{
   private static final boolean VERBOSE = true;

   @Test
   public final void testBoard()
   {
  //    StompleBoard stompleBoard = new StompleBoard();

 //     if (StompleBoard.getTrace())
         System.out.println(" public final void test()");

      //Number of players is between 2 and 6

      //      assertTrue(stompleBoard.getnumberOfPlayers() <= 6);

      //Frequency of each color in the board

      //      assertEqualsFrameFrequencyOfColors();

      //Frequency of each player in the list of players

      //      assertNamePlayers();

      //board game is not empty

      //      assertTrue(!(stompleBoard.getMarbleList().isEmpty()));

   }

   @Test
   public void testGameHasEqualFrequencyOfColors()
   {
      if (VERBOSE)
         System.out.println("Frequency of each color in the board.");

      StompleBoard stompleBoard = new StompleBoard();

      int freqOfColor;
      for (StompleColor iColor : StompleColor.values())
      {
         if (iColor != StompleColor.NONE)
         {
            List<StompleColor> marbleList = stompleBoard.getMarbleList();
            freqOfColor = Collections.frequency(marbleList, iColor);
            if (VERBOSE)
               System.out.println("Frequency of " + iColor + " is " + freqOfColor);
            assertEquals(freqOfColor, StompleColor.values().length - 1);
         }
      }
   }

   @Test
   public void testBoardHasNamedPlayers()
   {
//      if (StompleBoard.getTrace())
         System.out.println("Frequency of each player in the list of players");

      StompleBoard stompleBoard = new StompleBoard();

      ArrayList<StomplePlayer> playerList = stompleBoard.getPlayerList();

      assertEquals(6, playerList.size());

      ArrayList<StompleColor> playerColors = new ArrayList<>();

      for (StomplePlayer player : playerList)
      {
         playerColors.add(player.getColor());
      }

      for (StompleColor color : StompleColor.values())
      {
         if ((color == StompleColor.NONE) || (color == StompleColor.BLACK))
            continue;

         assertTrue(playerColors.contains(color));
      }
   }
}


