package com.doldolmeet.domain.fanMeeting.sse;

import com.doldolmeet.domain.fanMeeting.sse.SseController;
import com.doldolmeet.recording.controller.MyRecordingController;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.extern.slf4j.Slf4j;

public class MyTaskSecond implements Runnable{

    private String sessionId;
    private String username;
    private OpenVidu openvidu;
    Object lock = new Object();
    public MyTaskSecond(String sessionId, String username, OpenVidu openvidu) {
        this.sessionId = sessionId;
        this.username = username;
        this.openvidu = openvidu;
    }


    @Override
    public void run() {
        synchronized (lock) {
            try {
                SseController.networkChecker.put(sessionId,lock);
                long startTime = System.currentTimeMillis();
                //1분동안 기다린다.
                long timeLimit = 60000;
                lock.wait(timeLimit);
                long endTime = System.currentTimeMillis();
                long diff = endTime - startTime;
                if (diff < timeLimit) {
                    //네트워크 문제로 인해 끊긴 경우 진행중인 recording을 중지시킨다.
                    MyRecordingController.sessionRecordings.remove(sessionId);
                    String recordingId = MyRecordingController.sessionIdRecordingsMap.get(sessionId+username).getId();
                    this.openvidu.stopRecording(recordingId);
                    System.out.println("-------- recording has been stopped by network connection problem --------");

                    //누가 네트워크 문제로 끊겼는지 파악한다.
                    Map<String, Long> InnerMap = new HashMap<>();
                    InnerMap.put(sessionId, diff);
                    SseController.userDroppedByBadNetwork.put(username,InnerMap);
                    System.out.println("-------- user who has been dropped by network is detected --------");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (OpenViduJavaClientException e) {
                throw new RuntimeException(e);
            } catch (OpenViduHttpException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
