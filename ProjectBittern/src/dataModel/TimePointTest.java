package dataModel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import static dataModel.TimePoint.*;
import dataModel.TimePoint.*;

class TimePointTest {
	
	AnimalTrack chick1 = new AnimalTrack("howdy");
	
	
	TimePoint a = new TimePoint(0, 0, 19);
	TimePoint b = new TimePoint(4, 3, 19);
	TimePoint c = new TimePoint(1, 2, 3);
	
	/*
	 * This method doesn't work properly yet, need to fix.
	 */
	@Test
	public void test() {
//		chick1.add(a);
		a.setX(4);
		a.setY(3);
		chick1.add(a);
//		chick1.add(c);
		
		assertEquals(b, chick1.getTimePointAtTime(19));
	}

}
