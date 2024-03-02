package com.truongvu.myhotel.controller;

import com.truongvu.myhotel.exception.InvalidBookingRequestException;
import com.truongvu.myhotel.exception.ResourceNotFoundException;
import com.truongvu.myhotel.model.BookedRoom;
import com.truongvu.myhotel.model.Room;
import com.truongvu.myhotel.response.BookedRoomResponse;
import com.truongvu.myhotel.response.RoomResponse;
import com.truongvu.myhotel.service.BookedRoomService;
import com.truongvu.myhotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookedRoomController {
    @Autowired
    private final BookedRoomService bookedRoomService;
    @Autowired
    private final RoomService roomService;

    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookedRoomResponse>> getAllBookedRooms() {
        List<BookedRoom> bookings = bookedRoomService.getAllBookedRoom();
        List<BookedRoomResponse> bookedRoomResponses = new ArrayList<>();
        for(BookedRoom bookedRoom : bookings) {
            BookedRoomResponse bookedRoomResponse = getBookedRoomResponse(bookedRoom);
            bookedRoomResponses.add(bookedRoomResponse);
        }
        return ResponseEntity.ok(bookedRoomResponses);
    }

    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookedRoomByConfirmationCode(@PathVariable("confirmationCode") String confirmationCode) {
        try {
            BookedRoom bookedRoom = bookedRoomService.findByBookedRoomConfirmationCode(confirmationCode);
            BookedRoomResponse bookedRoomResponse = getBookedRoomResponse(bookedRoom);
            return ResponseEntity.ok(bookedRoomResponse);
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBookedRoom(
            @PathVariable Long roomId,
            @RequestBody BookedRoom bookedRoom) {
        try{
            String confirmationCode = bookedRoomService.saveBookedRoom(roomId, bookedRoom);
            return ResponseEntity.ok("Room booked successfully! Your booking code is : " + confirmationCode);
        } catch (InvalidBookingRequestException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(Long bookingId) {
        bookedRoomService.cancelBooking(bookingId);
    }

    private BookedRoomResponse getBookedRoomResponse(BookedRoom bookedRoom) {
        Room theRoom = roomService.getRoomById(bookedRoom.getRoom().getId()).get();
        RoomResponse roomResponse = new RoomResponse(theRoom.getId(), theRoom.getRoomType(), theRoom.getRoomPrice());
        return new BookedRoomResponse(
                bookedRoom.getBookingId(),
                bookedRoom.getCheckInDate(),
                bookedRoom.getCheckOutDate(),
                bookedRoom.getGuestFullName(),
                bookedRoom.getGuestEmail(),
                bookedRoom.getNumberOfAdults(),
                bookedRoom.getNumberOfChildren(),
                bookedRoom.getTotalGuests(),
                bookedRoom.getBookingConfirmationCode(),
                roomResponse
        );
    }
}
