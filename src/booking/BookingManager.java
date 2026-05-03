package booking;

import java.util.*;
import java.time.*;//get current/now date + time
import user.User;
import facility.Facility;
import storage.*;

public class BookingManager{
	
	Scanner input = new Scanner(System.in);

	private ArrayList<Booking> bookings;
	
	private ArrayList<Facility> facilities;
	
	private BookingStorage bookingStorage;
	
	private FacilityStorage facilityStorage;
	
	//constructor
	public BookingManager() {
		
		this.bookings = new ArrayList<>();
		this.facilities = new ArrayList<>();
		
		this.bookingStorage = new BookingStorage("booking.txt");
		this.facilityStorage = new FacilityStorage("facilities.txt");
		
	}
	
	//current userId
	private User currentUser;
	
	public void setCurrentUser(User user) {
		
		this.currentUser = user;
	}

	public boolean isBooked(String facilityId, TimeSlot timeSlot) {

		for (Booking b : bookings) {

			if (b.getFacilityId().equals(facilityId)) {

				if (b.getTimeSlot().overlaps(timeSlot)) {
					return true;
				}
			}
		}

		return false;
	}
	
	// (case 1) 
	public void createBooking() {
		
		String currentUserId = currentUser.getUserId();
		
		//bookingId, output: BXXX , 3int , not enough = fill with 0
		String bookingId = String.format("B%03d", bookings.size() + 1);
		
		//facilityId
		String trueFacilityId = null;
		
		while(true) {
			System.out.print("\nEnter facilityId(Ex:F001)/(Enter 0 to exit): ");
			String facilityId = input.nextLine();
			
			if (facilityId.equals("0")) {
				System.out.println("\nExit Creation.");
				return;
			}
			
			if(checkFacilityId(facilityId)) {
				
				trueFacilityId = facilityId;
				break;
			}
			System.out.println("Please Re-enter: ");
		}
		
		
		//timeSlot
		//must declare outside loops (cannot pass data if declare inside loops)
		LocalDate date = null;
		LocalTime startTime = null;
		LocalTime endTime = null;
		TimeSlot trueTimeSlot = null;
		
		while(true) {
			
			//check date
			while(true) {
				System.out.print("\nEnter date(Ex:2026-05-01): ");
				String inputDate = input.nextLine();
				
				try {
					
					date = LocalDate.parse(inputDate);
					
					if(date.isBefore(LocalDate.now())) {
						System.out.println("\nInvalid Date! (Past)");
						System.out.println("Please Re-enter: ");
						continue;	//restart from while loop header
					}
					break;
				}
				catch(Exception e) {
					System.out.println("\nFalse Date Format!");
					System.out.println("Please Re-enter: ");
				}
			}
			
			//check start time
			while(true) {
				System.out.print("\nEnter start time(Ex:10:00): ");
				String inputStartTime = input.nextLine();
				
				try {
					startTime = LocalTime.parse(inputStartTime);
					
					LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
					
					if(startDateTime.isBefore(LocalDateTime.now())) {
						System.out.println("\nInvalid Time! (Past)");
						System.out.println("Please Re-enter: ");
						continue;
					}
					break;
				}
				catch(Exception e) {
					System.out.println("\nFalse Time Format!");
					System.out.println("Please Re-enter: ");
				}
			}
			
			//check end time
			while(true) {			
				System.out.print("\nEnter end time(Ex:12:00): ");
				String inputEndTime = input.nextLine();
				
				try {
					endTime = LocalTime.parse(inputEndTime);
					
					LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
					
					if(endDateTime.isBefore(LocalDateTime.now())) {
						System.out.println("\nInvalid Time! (Past)");
						System.out.println("Please Re-enter: ");
						continue;
					}else if(endTime.isBefore(startTime)) {
						System.out.println("\nInvalid Time! (end time cannot before start time)");
						System.out.println("Please Re-enter: ");
						continue;
					}
					break;
				}
				catch(Exception e) {
					System.out.println("\nFalse Time Format!");
					System.out.println("Please Re-enter: ");
				}
			}
			
			
			try {
				TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
				
				if( checkConflict(null, trueFacilityId, timeSlot) ) {
					
					//save true timeSlot
					trueTimeSlot = timeSlot;
					
					// no conflict --> exit loop
					break;
				}
			}
			catch(Exception e) {
				System.out.println("\nFalse Time Format!");
				System.out.println("Please Re-enter: ");
			}
		}	
		
		
		//purpose
		System.out.print("\nEnter purpose: ");
		String purpose = input.nextLine();
		
		//status
		BookingStatus status = BookingStatus.PENDING;
		
		//createdTime
		LocalDateTime createdTime = LocalDateTime.now().withSecond(0).withNano(0);
		
		//lastModifiedTime
		LocalDateTime lastModifiedTime = LocalDateTime.now().withSecond(0).withNano(0);
		
		Booking b = new Booking(bookingId, currentUserId, trueFacilityId, trueTimeSlot, 
					purpose, status, createdTime, lastModifiedTime);
			
		bookings.add(b);
			
		saveBookings();
			
		System.out.println("\nBooking " + bookingId + " Created Successfully.");
		
	}
	
	// (case 2) 
	public void modifyBooking() {
		
		String currentUserId = currentUser.getUserId();
		
		//bookingId
		String trueBookingId = null;
		
		while(true) {
			boolean found = false;
			boolean privilege = false;
			
			System.out.print("\nEnter bookingId(Ex:B001)/(Enter 0 to exit): ");
			String bookingId = input.nextLine();
			
			if (bookingId.equals("0")) {
				System.out.println("\nExit Modification.");
				return;
			}
			
			if(checkBookingId(bookingId)) {
				
				for(Booking b : bookings) {
				
					if(b.getBookingId().equals(bookingId)) {
			
						found = true;
						
						if(!b.getUserId().equals(currentUserId)) {
							System.out.println("\nNo Privileges to Modify " + bookingId + "!");
							break;
						}
						else {
							privilege = true;
						}
						
						if(!b.canStatus()) {
							System.out.println("\nCannot Modify! (booking " + bookingId + " has already been " 
						+ b.getStatus().toString().toLowerCase() + ")");
							privilege = false;
							break;
						}
						else {
							privilege = true;
						}
						
						if(!b.canTime()) {
							System.out.println("\nCannot Modify! (booking " + bookingId + " has already passed)");
							privilege = false;
							break;
						}
						else {
							privilege = true;
						}
						
						break;
					}
				}
				
				if(!privilege) {
					return;	// stop this whole method
				}
				
				if(found) {
					trueBookingId = bookingId;
					break;
				}			
			}
		}
			
		
		//timeSlot
		//must declare outside loops (cannot pass data if declare inside loops)
		LocalDate date = null;
		LocalTime startTime = null;
		LocalTime endTime = null;
		TimeSlot trueTimeSlot = null;
		
		while(true) {
			
			//check date
			while(true) {
				System.out.print("\nEnter new date(Ex:2026-05-02): ");
				String inputDate = input.nextLine();
				
				try {
					
					date = LocalDate.parse(inputDate);
					
					if(date.isBefore(LocalDate.now())) {
						System.out.println("\nInvalid Date! (Past)");
						System.out.println("Please Re-enter: ");
						continue;	//restart from while loop header
					}
					break;
				}
				catch(Exception e) {
					System.out.println("\nFalse Date Format!");
					System.out.println("Please Re-enter: ");
				}
			}
			
			//check start time
			while(true) {
				System.out.print("\nEnter new start time(Ex:13:00): ");
				String inputStartTime = input.nextLine();
				
				try {
					startTime = LocalTime.parse(inputStartTime);
					
					LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
					
					if(startDateTime.isBefore(LocalDateTime.now())) {
						System.out.println("\nInvalid Time! (Past)");
						System.out.println("Please Re-enter: ");
						continue;
					}
					break;
				}
				catch(Exception e) {
					System.out.println("\nFalse Time Format!");
					System.out.println("Please Re-enter: ");
				}
			}
			
			//check end time
			while(true) {			
				System.out.print("\nEnter new end time(Ex:15:00): ");
				String inputEndTime = input.nextLine();
				
				try {
					endTime = LocalTime.parse(inputEndTime);
					
					LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
					
					if(endDateTime.isBefore(LocalDateTime.now())) {
						System.out.println("\nInvalid Time! (Past)");
						System.out.println("Please Re-enter: ");
						continue;
					}else if(endTime.isBefore(startTime)) {
						System.out.println("\nInvalid Time! (end time cannot before start time)");
						System.out.println("Please Re-enter: ");
						continue;
					}
					break;
				}
				catch(Exception e) {
					System.out.println("\nFalse Time Format!");
					System.out.println("Please Re-enter: ");
				}
			}
			
			
			try {
				TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
				
				//check new input
				if(checkConflict(trueBookingId, getFacilityIdByBookingId(trueBookingId), timeSlot)) {
				
					trueTimeSlot = timeSlot;
					break;
				}
			}
			catch(Exception e) {
				System.out.println("\nFalse Time Format!");
				System.out.println("Please Re-enter: ");
			}
		}		
		
		//purpose
		System.out.print("\nEnter new purpose: ");
		String purpose = input.nextLine();
		
		for(Booking b : bookings) {
			
			if(b.getBookingId().equals(trueBookingId)) {
				
				LocalDateTime lastModifiedTime = LocalDateTime.now().withSecond(0).withNano(0);
							
				b.modifyBooking(trueTimeSlot, purpose, lastModifiedTime);
							
				saveBookings();
				
				System.out.println("\nBooking " + trueBookingId + " Modified Successfully.");

				break;
			}
		}
	}
		
	// (case 3) 
	public void cancelBooking() {
		
		String currentUserId = currentUser.getUserId();
		
		//bookingId
		String trueBookingId;
		
		while(true) {
			
			boolean privilege = false;
			boolean found = false;
			
			System.out.print("\nEnter bookingId(Ex:B001)/(Enter 0 to exit): ");
			String bookingId = input.nextLine();
			
			if (bookingId.equals("0")) {
				System.out.println("\nExit Cancellation.");
				return;
			}
			
			if(checkBookingId(bookingId)) {
			
				for(Booking b : bookings) {
					
					if(b.getBookingId().equals(bookingId)) {
						
						found = true;
						
						if(!b.getUserId().equals(currentUserId)) {
							System.out.println("\nNo Privileges to Cancel " + bookingId + "!");
							break;
						}
						else {
							privilege = true;
						}
						
						if(!b.canStatus()) {
							System.out.println("\nCannot Cancel! (booking " + bookingId + " has already been " 
						+ b.getStatus().toString().toLowerCase() + ")");
							privilege = false;
							break;
						}
						else {
							privilege = true;
						}
						
						if(!b.canTime()) {
							System.out.println("\nCannot Cancel! (booking " + bookingId + " has already passed)");
							privilege = false;
							break;
						}
						else {
							privilege = true;
						}
						
						break;
					}
				}
				
				if(!privilege) {
					return;	// stop this whole method
				}
				
				if(found) {
					trueBookingId = bookingId;
					break;
				}
			}
		}
		
		for(Booking b : bookings) {
			
			if(b.getBookingId().equals(trueBookingId)) {
				
				b.setStatus(BookingStatus.CANCELLED);
				
				saveBookings();
				
				System.out.println("\nBooking " + trueBookingId + " Cancelled Successfully.");

				break;
			}
		}
	}
	
	// (case 4)
	public void showMyBookings(User currentUser){
		
		int i = 0;
		
		for(Booking b : bookings) {
			
			if(b.getUserId().equals(currentUser.getUserId())) {
				i++;
				System.out.println(b);
			}
		}
		
		if(i == 0) {
			System.out.println("\nYou do not have any booking yet.");
		}
		else {
			System.out.println("\nAll your bookings are shown.");
		}
	}

	
	//admin only (case 5) 
	public void showAllBookings() {
		
		for(Booking b : bookings) {
			System.out.println(b);
		}
		System.out.println("\nAll bookings are shown.");
	}
	
	//admin only (case 6)
	public void approveBooking() {
		
		//bookingId
		String trueBookingId;
		
		while(true) {
			
			boolean privilege = false;
			boolean found = false;
			
			System.out.print("\nEnter bookingId(Ex:B001)/(Enter 0 to exit): ");
			String bookingId = input.nextLine();
			
			if (bookingId.equals("0")) {
				System.out.println("\nExit Approval.");
				return;
			}
			
			if(checkBookingId(bookingId)) {
			
				for(Booking b : bookings) {
					
					if(b.getBookingId().equals(bookingId)) {
						
						found = true;
						
						if(!b.canStatus()) {
							System.out.println("\nCannot Approved! (booking " + bookingId + " has already been " 
						+ b.getStatus().toString().toLowerCase() + ")");
							
							break;
						}
						else {
							privilege = true;
						}
						
						if(!b.canTime()) {
							System.out.println("\nCannot Approved! (booking " + bookingId + " has already passed)");
							privilege = false;
							break;
						}
						else {
							privilege = true;
						}
						
						break;
					}
				}
				
				if(!privilege) {
					return;	// stop this whole method
				}
				
				if(found) {
					trueBookingId = bookingId;
					break;
				}
			}
		}
		
		for(Booking b : bookings) {
			
			if(b.getBookingId().equals(trueBookingId)) {
				
				b.setStatus(BookingStatus.APPROVED);
				
				saveBookings();
				
				System.out.println("\nBooking " + trueBookingId + " Approved.");

				break;
			}
		}
	}
		
	
	
	//admin only (case 7)
	public void rejectBooking() {

		//bookingId
		String trueBookingId;
		
		while(true) {
			
			boolean privilege = false;
			boolean found = false;
			
			System.out.print("\nEnter bookingId(Ex:B001)/(Enter 0 to exit): ");
			String bookingId = input.nextLine();
			
			if (bookingId.equals("0")) {
				System.out.println("\nExit Rejection.");
				return;
			}
			
			if(checkBookingId(bookingId)) {
			
				for(Booking b : bookings) {
					
					if(b.getBookingId().equals(bookingId)) {
						
						found = true;
						
						if(!b.canStatus()) {
							System.out.println("\nCannot Rejected! (booking " + bookingId + " has already been " 
						+ b.getStatus().toString().toLowerCase() + ")");
							
							break;
						}
						else {
							privilege = true;
						}
						
						if(!b.canTime()) {
							System.out.println("\nCannot Rejected! (booking " + bookingId + " has already passed)");
							privilege = false;
							break;
						}
						else {
							privilege = true;
						}
						
						break;
					}
				}
				
				if(!privilege) {
					return;	// stop this whole method
				}
				
				if(found) {
					trueBookingId = bookingId;
					break;
				}
			}
		}
		
		for(Booking b : bookings) {
			
			if(b.getBookingId().equals(trueBookingId)) {
				
				b.setStatus(BookingStatus.REJECTED);
				
				saveBookings();
				
				System.out.println("\nBooking " + trueBookingId + " Rejected.");

				break;
			}
		}
	}
	
	
	public boolean checkFacilityId(String facilityId) {
		
		boolean found = false;
		
		//check the input facilityId in the storage or not
		for(Facility f : facilities) {
			
			if(f.getFacilityId().equals(facilityId)) {
				
				found = true;
				break;
			}
		}
		
		if(!found) {	//the input facilityId not in the storage = false
			System.out.println("\nFalse facilityId!");
			return false;
		}
		else {
			return true;
		}
		
	}
	
	public boolean checkBookingId(String bookingId) {
		
		boolean found = false;
		
		for(Booking b : bookings) {
			
			if(b.getBookingId().equals(bookingId)) {
				
				found = true;
				break;
			}
		}
		
		if(!found) {
			System.out.println("\nFalse bookingId!");
			System.out.println("Please Re-enter: ");
			return false;
		}
		else {
			return true;
		}
		
	}
	
	
	public boolean checkConflict(String bookingId, String facilityId, TimeSlot timeSlot) {
	
		for(Booking b : bookings) {
			
			//just check PENDING & APPROVED
			if(b.getStatus().equals(BookingStatus.REJECTED) || b.getStatus().equals(BookingStatus.CANCELLED)) {
				
				continue;	//= skip this b, loops next again
			}
			
			//skip when modify, 
			//because new time maybe will conflict the old(actually will be replace)
			if(bookingId != null && b.getBookingId().equals(bookingId)) {
				continue;	//= skip
			}
			
			//skip when different facility
			if(!b.getFacilityId().equals(facilityId)) {
				continue;
			}
			
			//check when same facility
			if(b.getFacilityId().equals(facilityId)) {
				
				if(b.getTimeSlot().overlaps(timeSlot)) {
					System.out.println("\nTime Conflict!");
					System.out.println("Please Re-enter: ");
					return false;
				}
			}
		}
		
		System.out.println("\nNo Time Conflict Detected");
		return true;
		
	}

	
	public String getFacilityIdByBookingId(String bookingId) {
		
		for(Booking b : bookings) {
			
			if(b.getBookingId().equals(bookingId)) {
				
				return b.getFacilityId();
				
				//when return occur, (loop + this method) will stop
			}
		}
		
		//if not found
		return null;
	}
	
	
	public int getUserBookingStatistics(String userId) {
		
		int count = 0;
		
		for(Booking b : bookings) {
			if(b.getUserId().equals(userId)) {
				count++;
			}
		}
		return count;
	}
	
	public ArrayList<Booking> getUpcomingBookings(){
		
		ArrayList<Booking> result = new ArrayList<>();
		
		LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
		
		for(Booking b : bookings) {
			
			LocalDateTime startTime = LocalDateTime.of(
					b.getTimeSlot().getDate(), 
					b.getTimeSlot().getStartTime());
			
			//current booking within 12 hour
			if(startTime.isAfter(now) && 
					startTime.isBefore(now.plusHours(12))) {
				result.add(b);
			}
		}
		
		//Ascending order
		result.sort( (b1, b2) -> {
			LocalDateTime t1 = LocalDateTime.of(
					b1.getTimeSlot().getDate(), 
					b1.getTimeSlot().getStartTime());
			
			LocalDateTime t2 = LocalDateTime.of(
					b2.getTimeSlot().getDate(), 
					b2.getTimeSlot().getStartTime());
			
			//is t1 earlier than t2?
			return t1.compareTo(t2);
		} );
		
		return result;
	}
	

	//Map = a refer table (through Key get Value)
	public Map<String, Integer> getPeakBookingHours() {
	
		//HashMap = a statistic table
		Map<String, Integer> hourCount = new HashMap<>();
		
		for(Booking b : bookings) {
			
			LocalTime startTime = b.getTimeSlot().getStartTime();
			LocalTime endTime = b.getTimeSlot().getEndTime();
			
			//loop from start until end（not include end）
			//Ex: "10:00 - 12:00", hour = "10", "11"
			while(startTime.isBefore(endTime)) {
				
				//cut hour only("10:30" → "10")
				String hour = String.format("%02d", startTime.getHour());
				
				//hourCount.getOrDefault(hour, 0) + 1) 
				//= if Map have the hour --> (the hour's count) then + 1
				//= if Map haven't the hour --> (let the hour's count = 0) then + 1
				hourCount.put(hour, hourCount.getOrDefault(hour, 0) + 1);
				
				startTime = startTime.plusHours(1);
			}
		}
		
		return hourCount;
		//Ex: 10 = 5
		//hour = hourCount
	}
	
	
	public void generatePeakBookingReport() {
		
		//call getPeakBookingHours() method to get result
		Map<String, Integer> hourCount = getPeakBookingHours();
		
		//TreeMap = Ascending
		Map<String, Integer> report = new TreeMap<>(hourCount);
		
		System.out.println("\n====== Peak Booking Report ======");
		
		//entry = (Key + Value) , Ex: "10" --> 5
		for(Map.Entry<String, Integer> entry : report.entrySet()) {
			
			System.out.println(entry.getKey() + ":00 --> " + 
			entry.getValue() + " bookings");
			
			//Ex: 10:00 --> 5 bookings
		}
		System.out.println("\n=================================");
	}
	
	
	public void loadFacilities() {
		
		facilities.clear();
		
		facilities = facilityStorage.load();
		
	}

	public void loadBookings() {
		
		bookings.clear();
		
		bookings = bookingStorage.load();
	
	}

	public void saveBookings() {
		
		bookingStorage.save(bookings);
	}
	
	
}
