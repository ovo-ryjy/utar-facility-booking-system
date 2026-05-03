package booking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class TimeSlot {

	private final LocalDate date;
	private final LocalTime startTime;
	private final LocalTime endTime;

	// constructor
	public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
		this.date = Objects.requireNonNull(date, "Date cannot be empty");
		this.startTime = Objects.requireNonNull(startTime, "Start time cannot be empty");
		this.endTime = Objects.requireNonNull(endTime, "End time cannot be empty");

		if (!startTime.isBefore(endTime)) {
			throw new IllegalArgumentException("Start time must be before end time");
		}
	}

	// check overlap
	public boolean overlaps(TimeSlot timeSlot) {
		
		if (timeSlot == null) 
			return false;
		
		return this.date.equals(timeSlot.date) &&
			       this.startTime.isBefore(timeSlot.endTime) &&
			       timeSlot.startTime.isBefore(this.endTime);

	}

	// getters
	public LocalDate getDate() { 
		return date; 
	}
	
	public LocalTime getStartTime() { 
		return startTime; 
	}
	
	public LocalTime getEndTime() { 
		return endTime; 
	}
	
}
