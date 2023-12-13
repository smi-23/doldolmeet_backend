package com.doldolmeet.domain.waitRoom.chat.controller;

import com.doldolmeet.domain.waitRoom.chat.dto.ChatRoomDto;
import com.doldolmeet.domain.waitRoom.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoomDto> room() {
        return chatRoomService.findAllRoom();
    }

    @PostMapping("/room")
    @ResponseBody
    public ChatRoomDto createRoom(@RequestParam String name) {
        return chatRoomService.createChatRoom(name);
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoomDto roomInfo(@PathVariable String roomId) {
        return chatRoomService.findRoomById(roomId);
    }

    //    // 대기방 입장
//    @GetMapping("/room/enter/{roomId}")
//    public String roomDetail(Model model, @PathVariable String roomId) {
//        model.addAttribute("roomId", roomId);
//        return "/roomdetail";
//    }

//    // 대기방 입장할 때마다 입장한 팬 저장
//    @GetMapping("/room/enter/{roomId}")
//    public String roomDetail(Model model, @PathVariable String roomId) {
//        model.addAttribute("roomId", roomId);
//        return "/chat/roomdetail";
//    }

    // 팬 대기방 입장
//    @GetMapping("waitRoom/enter/{fanMeetingId}")
//    public String getWaitRoomId(@PathVariable Long fanMeetingId, HttpServletRequest request) {
//        // getWaitRoomId 메서드: fanMeetingId와 reqeust를 받아서 roomId 반환
//        return chatRoomRepository.getWaitRoomId(fanMeetingId, request);
//    }

    // 특정 채팅방 조회
    // 일단, 로그인 되어 있는 상태
//    @GetMapping("/room/{fanMeetingId}")
//    @ResponseBody
//    public Message enterRoom(@PathVariable Long fanMeetingId, HttpServletRequest request) {
//        Message message = new Message();
//
//        Boolean result = chatService.enterRoom(fanMeetingId, request);
//
//        if (result) {
//            message.build(200, "success");
//        }
//        else {
//            message.build(400, "fail");
//        }
//
//        return message;
//    }

}
