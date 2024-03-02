package com.truongvu.myhotel.controller;

import com.truongvu.myhotel.exception.PhotoRetrievalException;
import com.truongvu.myhotel.exception.ResourceNotFoundException;
import com.truongvu.myhotel.model.BookedRoom;
import com.truongvu.myhotel.model.Room;
import com.truongvu.myhotel.response.BookedRoomResponse;
import com.truongvu.myhotel.response.RoomResponse;
import com.truongvu.myhotel.service.BookedRoomService;
import com.truongvu.myhotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {
    @Autowired
    private RoomService roomService;
    @Autowired
    private BookedRoomService bookedRoomService;

    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse roomResponse = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(), savedRoom.getRoomPrice());
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room-types")
    public List<String> getRoomType() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for(Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable("roomId") Long roomId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) BigDecimal roomPrice,
            @RequestParam(required = false) MultipartFile photo) throws IOException, SQLException {
        byte[] photoBytes = photo != null && !photo.isEmpty() ? photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;
        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        theRoom.setPhoto(photoBlob);
        RoomResponse roomResponse = getRoomResponse(theRoom);
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    // Cover Room to RoomResponse
    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookedRooms = getAllBookedRoomsByRoomId(room.getId());
//        List<BookedRoomResponse> bookedRoomResponses = bookedRooms
//                .stream()
//                .map(bookedRoom -> new BookedRoomResponse(bookedRoom.getBookingId(),bookedRoom.getCheckInDate(),bookedRoom.getCheckOutDate(), bookedRoom.getBookingConfirmationCode()))
//                .toList();
        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();

        if(photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException exception) {
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }

        return new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice(), room.isBooked(), photoBytes);
    }

    // Get all booking of this roomID
    private List<BookedRoom> getAllBookedRoomsByRoomId(Long roomId) {
        return bookedRoomService.getAllBookedRoomsByRoomId(roomId);
    }
}

