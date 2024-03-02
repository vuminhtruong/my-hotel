package com.truongvu.myhotel.repository;

import com.truongvu.myhotel.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookedRoomRepository extends JpaRepository<BookedRoom, Long> {
    BookedRoom findByBookingConfirmationCode(String confirmationCode);
    List<BookedRoom> findByRoomId(Long roomId);
}
