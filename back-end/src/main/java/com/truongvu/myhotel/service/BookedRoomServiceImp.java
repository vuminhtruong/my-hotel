package com.truongvu.myhotel.service;

import com.truongvu.myhotel.exception.InvalidBookingRequestException;
import com.truongvu.myhotel.model.BookedRoom;
import com.truongvu.myhotel.model.Room;
import com.truongvu.myhotel.repository.BookedRoomRepository;
import com.truongvu.myhotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookedRoomServiceImp implements BookedRoomService{
    @Autowired
    private final BookedRoomRepository bookedRoomRepository;
    @Autowired
    private final RoomService roomService;
    @Override
    public List<BookedRoom> getAllBookedRoom() {
        return bookedRoomRepository.findAll();
    }

    @Override
    public List<BookedRoom> getAllBookedRoomsByRoomId(Long roomId) {
        return bookedRoomRepository.findByRoomId(roomId);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        bookedRoomRepository.deleteById(bookingId);
    }

    @Override
    public String saveBookedRoom(Long roomId, BookedRoom bookedRoom) {
        if(bookedRoom.getCheckOutDate().isBefore(bookedRoom.getCheckInDate())) {
            throw new InvalidBookingRequestException("Check -in date must come before check-out date");
        }
        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookedRooms = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookedRoom, existingBookedRooms);
        if(roomIsAvailable) {
            room.addBooking(bookedRoom);
            bookedRoomRepository.save(bookedRoom);
        } else {
            throw new InvalidBookingRequestException("Sorry, This room is not available for the selected dates;");
        }
        return bookedRoom.getBookingConfirmationCode();
    }

    @Override
    public BookedRoom findByBookedRoomConfirmationCode(String confirmationCode) {
        return bookedRoomRepository.findByBookingConfirmationCode(confirmationCode);
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookedRooms) {
        return existingBookedRooms.stream()
                .noneMatch(
                        existingBooking ->
                                bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
                                        || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                                        || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                                        && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
                                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
                                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                        && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))

                                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))

                                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                        && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
                        );
    }
}
