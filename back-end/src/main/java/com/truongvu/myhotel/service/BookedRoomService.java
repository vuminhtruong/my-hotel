package com.truongvu.myhotel.service;

import com.truongvu.myhotel.model.BookedRoom;

import java.util.List;

public interface BookedRoomService {
    List<BookedRoom> getAllBookedRoomsByRoomId(Long roomId);

    void cancelBooking(Long bookingId);

    String saveBookedRoom(Long roomId, BookedRoom bookedRoom);

    BookedRoom findByBookedRoomConfirmationCode(String confirmationCode);

    List<BookedRoom> getAllBookedRoom();
}
