package dataModel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import static dataModel.TimePoint.*;
import dataModel.TimePoint.*;

class TimePointTest {
	
	AnimalTrack chick1 = new AnimalTrack("howdy");
	
	
	TimePoint a = new TimePoint(0, 0, 19);
	TimePoint b = new TimePoint(4, 3, 19);
	
	@Test
	public void test() {
		chick1.add(a);
		a.setX(4);
		a.setY(3);
		
		assertEquals(b, chick1.getTimePointAtTime(19));
	}

}
