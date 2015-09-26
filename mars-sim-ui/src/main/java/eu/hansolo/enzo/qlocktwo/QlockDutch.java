/*
 * Copyright (c) 2015 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.enzo.qlocktwo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by
 * User: hansolo
 * Date: 27.02.13
 * Time: 15:44
 */
public class QlockDutch implements Qlock {
    private static final QlockTwo.Language LANGUAGE = QlockTwo.Language.DUTCH;
    private static final String[][] MATRIX = {
        {"H","E", "T", "K", "I", "S", "A", "V", "I", "I", "F"},
        {"T", "I", "E", "N", "B", "T", "Z", "V", "O", "O","R"},
        {"O", "V", "E", "R","M", "E", "K", "W", "A", "R", "T"},
        {"H", "A", "L", "F", "S", "P", "W", "O", "V", "E", "R"},
        {"V", "O", "O", "R", "T", "H", "G", "É", "É", "N", "S"},
        {"T", "W", "E", "E", "P", "V", "C", "D", "R", "I", "E"},
        {"V", "I", "E", "R", "V", "I", "I", "F", "Z", "E", "S"},
        {"Z", "E", "V", "E", "N", "O", "N", "E", "G", "E", "N"},
        {"A", "C", "H", "T", "T", "I", "E", "N", "E", "L", "F"},
        {"T", "W", "A", "A", "L", "F", "B", "F", "U", "U", "R"}
    };
    private final ConcurrentHashMap<Integer, String> LOOKUP;
    private List<QlockWord> timeList;


    public QlockDutch() {
        LOOKUP = new ConcurrentHashMap<>();
        LOOKUP.putAll(QlockTwo.Language.DUTCH.getLookup());
        timeList = new ArrayList<>(10);
    }

    @Override public String[][] getMatrix() {
        return MATRIX;
    }

    @Override public List<QlockWord> getTime(int minute, int hour) {
        if (hour > 12) {
            hour -= 12;
        }
        if (hour <= 0) {
            hour += 12;
        }

        if (minute > 60) {
            minute -= 60;
            hour++;
        }
        if (minute < 0) {
            minute += 60;
            hour--;
        }

        minute -= minute % 5;

        timeList.clear();

        timeList.add(QlockLanguage.HET);
        timeList.add(QlockLanguage.IS);
        switch (minute) {
            case 0:
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                timeList.add(QlockLanguage.UUR);
                break;
            case 5:
                timeList.add(QlockLanguage.VIJF1);
                timeList.add(QlockLanguage.OVER);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                break;
            case 10:
                timeList.add(QlockLanguage.TIEN);
                timeList.add(QlockLanguage.OVER);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                break;
            case 15:
                timeList.add(QlockLanguage.KWART);
                timeList.add(QlockLanguage.OVER1);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                break;
            case 20:
                timeList.add(QlockLanguage.TIEN);
                timeList.add(QlockLanguage.VOOR);
                timeList.add(QlockLanguage.HALF);
                addHour(timeList, hour);
                break;
            case 25:
                timeList.add(QlockLanguage.VIJF1);
                timeList.add(QlockLanguage.VOOR);
                timeList.add(QlockLanguage.HALF);
                addHour(timeList, hour);
                break;
            case 30:
                timeList.add(QlockLanguage.HALF);
                addHour(timeList, hour);
                break;
            case 35:
                timeList.add(QlockLanguage.VIJF1);
                timeList.add(QlockLanguage.OVER);
                timeList.add(QlockLanguage.HALF);
                addHour(timeList, hour);
                break;
            case 40:
                timeList.add(QlockLanguage.TIEN);
                timeList.add(QlockLanguage.OVER);
                timeList.add(QlockLanguage.HALF);
                addHour(timeList, hour);
                break;
            case 45:
                timeList.add(QlockLanguage.KWART);
                timeList.add(QlockLanguage.VOOR1);
                addHour(timeList, hour);
                break;
            case 50:
                timeList.add(QlockLanguage.TIEN);
                timeList.add(QlockLanguage.VOOR);
                addHour(timeList, hour);
                break;
            case 55:
                timeList.add(QlockLanguage.VIJF1);
                timeList.add(QlockLanguage.VOOR);
                addHour(timeList, hour);
                break;
        }

        return timeList;
    }

    @Override public QlockTwo.Language getLanguage() {
        return LANGUAGE;
    }

    private void addHour(List<QlockWord> timeList, final int HOUR) {
        if (HOUR == 12) {
            timeList.add(QlockLanguage.EEN);
        } else if (HOUR == 10 || HOUR + 1 == 10) {
            timeList.add(QlockLanguage.TIEN1);
        } else if (HOUR + 1 == 5) {
            timeList.add(QlockLanguage.VIJF);
        } else {
            timeList.add(QlockLanguage.valueOf(LOOKUP.get(HOUR + 1)));
        }
    }

    private enum QlockLanguage implements QlockWord {
        EEN(4, 7, 9),
        TWEE(5, 0, 3),
        DRIE(5, 7, 10),
        VIER(6, 0, 3),
        VIJF1(0, 7, 10),
        VIJF(6, 4, 7),
        ZES(6, 8, 10),
        ZEVEN(7, 0, 4),
        ACHT(8, 0, 3),
        NEGEN(7, 6, 10),
        TIEN(1, 0, 3),
        TIEN1(8, 4, 7),
        ELF(8, 9, 10),
        TWAALF(9, 0, 5),
        HET(0, 0, 2),
        IS(0, 4, 5),
        OVER(2, 0, 3),
        OVER1(3, 7, 10),
        KWART(2, 6, 10),
        VOOR(1, 7, 10),
        VOOR1(4, 0, 3),
        HALF(3, 0, 3),
        UUR(9, 8, 10);
        private final int ROW;
        private final int START;
        private final int STOP;

        private QlockLanguage(final int ROW, final int START, final int STOP) {
            this.ROW = ROW;
            this.START = START;
            this.STOP = STOP;
        }

        @Override public int getRow() {
            return ROW;
        }

        @Override public int getStart() {
            return START;
        }

        @Override public int getStop() {
            return STOP;
        }
    }
}
