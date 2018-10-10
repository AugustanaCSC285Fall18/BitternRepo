
package dataModel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import static dataModel.TimePoint.*;
import dataModel.TimePoint.*;

class TimePointTest {
	
	TimePoint a = new TimePoint(0, 0, 19);
	TimePoint b = new TimePoint(4, 3, 19);
	TimePoint c = new TimePoint(1, 2, 3);
	
	@Test
	public void testForGetX() {
		assertEquals(0, a.getX());
		assertEquals(4, b.getX());
		assertEquals(1, c.getX());
	}
	
	@Test
	public void testForGetY() {
		assertEquals(0, a.getY());
		assertEquals(3, b.getY());
		assertEquals(2, c.getY());
	}
	
	@Test
	public void testForGetFrameNum() {
		assertEquals(19, a.getFrameNum());
		assertEquals(19, b.getFrameNum());
		assertEquals(3, c.getFrameNum());
	}
	
	

}
