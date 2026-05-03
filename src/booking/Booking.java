package booking;

// get current/now date + time
import java.time.LocalDateTime;

public class Booking {

	private String bookingId;
	private String userId;
	private String facilityId;
	private TimeSlot timeSlot;
	private String purpose;
	private BookingStatus status;
	private LocalDateTime createdTime;
	private LocalDateTime lastModifiedTime;
	
	//constructor
	public Booking(String bookingId, String userId, String facilityId, TimeSlot timeSlot, 
			String purpose, BookingStatus status, LocalDateTime createdTime, LocalDateTime lastModifiedTime) {
		this.bookingId = bookingId;
		this.userId = userId;
		this.facilityId = facilityId;
		this.timeSlot = timeSlot;
		this.purpose = purpose;
		this.status = status;
		this.createdTime = createdTime;
		this.lastModifiedTime = lastModifiedTime;
	}
	
	//print out
	@Override
	public String toString() {
		
		return "Booking ID: " + bookingId + 
				"\nUser ID: " + userId + 
				"\nFacility ID: " + facilityId +
				"\nDate: " + timeSlot.getDate() + 
				"\nTime: " + timeSlot.getStartTime() + " - " + timeSlot.getEndTime() + 
				"\nStatus: " + status + 
				"\n--------------------";
		
	}
	
	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public boolean canStatus() {
		if(status.equals(BookingStatus.PENDING)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean canTime() {
		
		// get current/now date + time
		LocalDateTime now = LocalDateTime.now();
		
		// get booking date + time
		LocalDateTime end = LocalDateTime.of(timeSlot.getDate(), timeSlot.getEndTime());
		
		// now date + time > booking end time
		if(now.isAfter(end)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void modifyBooking(TimeSlot timeSlot, String purpose, LocalDateTime lastModifiedTime) {
		this.timeSlot = timeSlot;
		this.purpose = purpose;
		this.lastModifiedTime = lastModifiedTime;
	}
	
	
	//getter
	public String getBookingId() {
		return bookingId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getFacilityId() {
		return facilityId;
	}
	
	public TimeSlot getTimeSlot() {
		return timeSlot;
	}
 
	public String getPurpose() {
		return purpose;
	}
	
	public BookingStatus getStatus() {
		return status;
	}
	
	public LocalDateTime getCreatedTime() {
		return createdTime;
	}
	
	public LocalDateTime getLastModifiedTime() {
		return lastModifiedTime;
	}
	
}
