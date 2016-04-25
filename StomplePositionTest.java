package us.ihmc.games.stomple;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import us.ihmc.tools.testing.MutationTestingTools;

public class StomplePositionTest
{
   @Test
   public final void testGetAdjacentPosition()
   {
      StomplePosition position = new StomplePosition(5, 7);
      
      assertEquals(5, position.getLine());
      assertEquals(7, position.getColumn());
      
      StomplePosition northPosition = position.getAdjacentPosition(StompleDirection.NORTH);
      assertEquals(4, northPosition.getLine());
      assertEquals(7, northPosition.getColumn());
      
   }
   
   public static void main(String[] args) throws URISyntaxException, IOException
   {
      String targetTests = "us.ihmc.games.stomple.StomplePositionTest";
      String targetClasses = "us.ihmc.games.stomple.StomplePosition";
      MutationTestingTools.doPITMutationTestAndOpenResult(targetTests, targetClasses);
   }

}
