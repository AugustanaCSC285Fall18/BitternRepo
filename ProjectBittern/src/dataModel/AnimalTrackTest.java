package dataModel;

import org.junit.Test;
import static org.junit.Assert.*;


public class AnimalTrackTest {
	
	
	AnimalTrack chick1 = new AnimalTrack("chick 1");
	
	AnimalTrack chick2 = new AnimalTrack("Not chick 1");
	
	TimePoint point1 = new TimePoint(100,100, 3456);
	TimePoint point2 = new TimePoint(200,200, 6789);
	TimePoint point3 = new TimePoint(300,300, 1234);
	
	
	
	@Test
	public void testForChickID () {
		assertEquals("chick 1", chick1.getID());
		assertEquals("Not chick 1", chick2.getID());
	}
	
	@Test
	public void testForGetTimePointAtIndex() {
		chick1.add(point1);
		chick1.add(point2);
		chick1.add(point3);
		
		chick2.add(point3);
		chick2.add(point1);
		chick2.add(point2);
		chick2.add(point2);
		
		assertEquals(point1, chick1.getTimePointAtIndex(0));
		assertEquals(point2, chick1.getTimePointAtIndex(1));
		assertEquals(point3, chick1.getTimePointAtIndex(2));
		
		assertEquals(point3, chick2.getTimePointAtIndex(0));
		assertEquals(point1, chick2.getTimePointAtIndex(1));
		assertEquals(point2, chick2.getTimePointAtIndex(2));
		assertEquals(3, chick2.getPositions().size());
	}
	
	

}
