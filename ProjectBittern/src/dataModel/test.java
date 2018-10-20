package dataModel;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		AnimalTrack chick1 = new AnimalTrack("chick 1");
		
		AnimalTrack chick2 = new AnimalTrack("Not chick 1");
		
		TimePoint point1 = new TimePoint(100,100, 3456);
		TimePoint point2 = new TimePoint(200,200, 6789);
		TimePoint point3 = new TimePoint(300,300, 1234);
		
		chick1.add(point1);
		chick1.add(point2);
		
	}

}
