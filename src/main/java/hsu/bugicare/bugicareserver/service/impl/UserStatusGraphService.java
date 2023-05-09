package hsu.bugicare.bugicareserver.service.impl;

import hsu.bugicare.bugicareserver.domain.Sleep;
import hsu.bugicare.bugicareserver.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserStatusGraphService {
    private final UserStatusRepository userStatusRepository;

    private int nowSecond;
    private int nowMinute;
    private int nowHour;

    private final int minusNum = -1;

    private int dateNum;

    private int n, m;

    @Autowired
    public UserStatusGraphService(UserStatusRepository userStatusRepository) {
        this.userStatusRepository = userStatusRepository;
    }


    // date 값 = day, week, month
    // 하루 or 일주일 or 한 달 동안의 어르신 취침시간
    public List<String> getCount(String date) {
        List<Sleep> sleep = null;

        // 반환할 String 배열, dayCount는 6개의 인자를, weekCount는 7개의 인자를, monthCount는 28개의 인자를 가진 String 배열이다.
        List<String> result = new ArrayList<>();
        int s = 0;

        // 현재 시, 분, 초 알아오기
        nowSecond = LocalTime.now().getSecond();
        nowMinute = LocalTime.now().getMinute();
        nowHour = LocalTime.now().getHour();

        // dateNum = 주, 월, 일 구분 변수, 하루 = 6, 일주일 = 7, 한 달 = 28 개의 배열 생성
        // 1시간 = 5초, 6시간 = 30초, 하루 = 120초

        // 하루(현재부터 6시간 전까지만)의 냉장고 문 열림 횟수
        if(date.equals("day")) {
            result = null; // 이전 값 초기화

            dateNum = 6;
            n = nowSecond / 5;

            // 하루(현재부터 6시간 전까지만)의 배열이 생성
            for(int i = 0; i < dateNum; i++) {
                m = (n*5 - i*5 < 0) ? (60 + n*5 - i*5)  : (n*5 - i*5);

                if(i == 0){
                    sleep = userStatusRepository.findDay(m, nowSecond, 0);
                }
                if(n*5 - i*5 < 0) {
                    sleep = userStatusRepository.findDay(m, m + 4, -1);
                }
                else{
                    sleep = userStatusRepository.findDay(m, m + 4, 0);
                }
                // sleep에 하나라도 들어있으면 주무시는 것이므로 1 전송 아니면 0
                result.add(String.valueOf(sleep.size() != 0 ? 1 : 0));
            }

            // 배열 반환
            return result;
        }
        else {
            dateNum = (date.equals("week") ? 7 : 28);

            // 주 or 월 이냐에 따라서 배열이 생성
            for(int i = 1; i <= (dateNum * 2); i++) {
                // NN시 1분 이상일 경우
                if(nowMinute - i >= 0) {
                    sleep = userStatusRepository.findWeekOrMonth(nowMinute - i + 1, 0);
                }

                // NN시 1분 미만일 경우 -N분이 되지 않도록 처리 && 시(Hour) 변경
                else {
                    // 12시일 경우 일/시 모두 변경
                    if(nowHour == 0) {
                        sleep = userStatusRepository.findWeekOrMonthAndChangeDay(60 + nowMinute - i + 1);
                    }
                    // 아닌 경우 시(Hour)만 변경
                    else {
                        sleep = userStatusRepository.findWeekOrMonth(60 + nowMinute - i + 1, minusNum);
                    }
                }

                s += sleep.size();

                // 하루 = 2분이므로 00분 ~ 1분, 2분 ~ 3분으로 나누기 때문에
                // 현재 NN시 3분이라면 00분 ~ 1분, 2분 ~ 3분 각각을 더해서 반환,
                // 현재 NN시 2분이라면 00분 ~ 1분, 2분 각각을 더해서 반환
                // 즉, 현재 짝수 분이라면 현재 분의 열림 횟수만 반환
                // 현재 홀수 분이라면 전 짝수 분까지의 열림 횟수를 더해서 반환
                if((nowMinute - i + 1) % 2 == 0) {
                    result.add(String.valueOf(s));
                    s = 0;
                }
            }
            // 배열 반환
            return result;
        }
    }
}