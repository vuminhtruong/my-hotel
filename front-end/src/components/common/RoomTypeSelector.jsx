import React, { useEffect, useState } from "react"
import { getRoomTypes } from "../util/ApiFunctions"

const RoomTypeSelector = ({handleRoomInputChange, newRoom}) => {
    const [roomTypes, setRoomTypes] = useState([""])
    const [showNewRoomTypeInput, setShowNewRoomTypeInput] = useState(false)
    const [newRoomTypes, setNewRoomTypes] = useState("")

    useEffect(() => {
        getRoomTypes().then((data) => {
            setRoomTypes(data)
        })
    }, [])

    const handleNewRoomInputChange = (e) => {
        setNewRoomTypes(e.target.value);
    }

    const handleAddNewRoomType = () => {
        if(newRoomTypes !== "") {
            setRoomTypes([...roomTypes, newRoomTypes])
            setNewRoomTypes("")
            setShowNewRoomTypeInput(false)
        }
    }

    return (
        <>
        {roomTypes.length > 0 && (
            <div>
                <select 
                id="roomType" 
                name="roomType" 
                value={newRoom.roomTypes} 
                onChange={(e) => {
                    if(e.target.value == "Add New") {
                        setShowNewRoomTypeInput(true)
                    } else {
                        handleRoomInputChange(e)
                    }
                }}>
                    <option value={""}>Select a room type</option>
                    <option value={"Add New"}>Add New</option>
                    {
                        roomTypes.map((type, index) => (
                            <option key={index} value={type}>
                                {type}
                            </option>
                        ))
                    }
                </select>
                {
                    showNewRoomTypeInput && (
                        <div className="input-group">
                            <input
                            className="form-control"
                            type="text"
                            placeholder="Enter a new room type"
                            onChange={handleNewRoomInputChange}
                            />
                            <button className="btn btn-hotel " type="button" onClick={handleAddNewRoomType}>Add</button>
                        </div>
                    )
                }
            </div>
        )}
        </>
    )
}

export default RoomTypeSelector