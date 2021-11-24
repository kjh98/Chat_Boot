package com.clone.chat;

import com.clone.chat.vo.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController // 데이터 리턴 서버
public class ChatController {

    private final ChatRepository chatRepository;

    //귓속말 할때 사용용
    @CrossOrigin
    @GetMapping(value = "/sender/{sender}/receiver/{receiver}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chat> getMsg(@PathVariable String sender, @PathVariable String receiver) {
        return chatRepository.mFindBySender(sender, receiver)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @CrossOrigin
    @GetMapping(value = "/chat/roomNum/{roomNum}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chat> findByRoomNum(@PathVariable Integer roomNum) {
        return chatRepository.mFindByRoomNum(roomNum)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @CrossOrigin
    @PostMapping("/chat")
    public Mono<Chat> setMsg(@RequestBody Chat chat) {
        chat.setCreateAt(LocalDateTime.now());
        return chatRepository.save(chat);
    }


    List<Room> roomList = new ArrayList<Room>();
    static int roomNumber = 0;


    @GetMapping("/chat")
    public ModelAndView chat() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("chat");
        return mv;
    }

    /**
     * 방 페이지
     *
     * @return
     */
    @GetMapping("/room")
    public ModelAndView room() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("room");
        return mv;
    }

    /**
     * 방 생성하기
     *
     * @param params
     * @return
     */
    @RequestMapping("/createRoom")
    public @ResponseBody
    List<Room> createRoom(@RequestParam HashMap<Object, Object> params) {
        String roomName = (String) params.get("roomName");
        if (roomName != null && !roomName.trim().equals("")) {
            Room room = new Room();
            room.setRoomNumber(++roomNumber);
            room.setRoomName(roomName);
            roomList.add(room);
        }
        return roomList;
    }

    /**
     * 방 정보가져오기
     *
     * @param params
     * @return
     */
    @RequestMapping("/getRoom")
    public @ResponseBody
    List<Room> getRoom(@RequestParam HashMap<Object, Object> params) {
        return roomList;
    }

    /**
     * 채팅방
     *
     * @return
     */
    @RequestMapping("/moveChating")
    public ModelAndView chating(@RequestParam HashMap<Object, Object> params) {
        ModelAndView mv = new ModelAndView();
        int roomNumber = Integer.parseInt((String) params.get("roomNumber"));
        List<Room> new_list = roomList.stream().filter(o -> o.getRoomNumber() == roomNumber).collect(Collectors.toList());
        if (new_list != null && new_list.size() > 0) {
            mv.addObject("roomName", params.get("roomName"));
            mv.addObject("roomNumber", params.get("roomNumber"));
            mv.setViewName("chat");
        } else {
            mv.setViewName("room");
        }
        return mv;
    }

    @Controller
    @RequestMapping("/hello")
    public class HelloController {
        @GetMapping
        public String hello() {
            return "room.html";
        }
    }

}
