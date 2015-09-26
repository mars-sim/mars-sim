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
 * Date: 28.02.13
 * Time: 07:53
 */
public class QlockSpanish implements Qlock {
    private static final QlockTwo.Language LANGUAGE = QlockTwo.Language.SPANISH;
    private static final String[][] MATRIX = {
        {"E", "S", "O", "N", "E", "L", "A", "S", "U", "N", "A"},
        {"D", "O", "S", "I", "T", "R", "E", "S", "O", "R", "E"},
        {"C", "U", "A", "T", "R", "O", "C", "I", "N", "C", "O"},
        {"S", "E", "I", "S", "A", "S", "I", "E", "T", "E", "N"},
        {"O", "C", "H", "O", "N", "U", "E", "V", "E", "Y", "O"},
        {"L", "A", "D", "I", "E", "Z", "S", "O", "N", "C", "E"},
        {"D", "O", "C", "E", "L", "Y", "M", "E", "N", "O", "S"},
        {"O", "V", "E", "I", "N", "T", "E", "D", "I", "E", "Z"},
        {"V", "E", "I", "N", "T", "I", "C", "I", "N", "C", "O"},
        {"M", "E", "D", "I", "A", "C", "U", "A", "R", "T", "O"}
    };
    private final ConcurrentHashMap<Integer, String> LOOKUP;
    private List<QlockWord> timeList;


    public QlockSpanish() {
        LOOKUP = new ConcurrentHashMap<>();
        LOOKUP.putAll(QlockTwo.Language.SPANISH.getLookup());
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

        minute -= minute%5;

        timeList.clear();

        switch (minute)
        {
            case 0:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                break;
            case 5:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                timeList.add(QlockLanguage.Y);
                timeList.add(QlockLanguage.CINCO1);
                break;
            case 10:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                timeList.add(QlockLanguage.Y);
                timeList.add(QlockLanguage.DIEZ1);
                break;
            case 15:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                timeList.add(QlockLanguage.Y);
                timeList.add(QlockLanguage.CUARTO);
                break;
            case 20:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                timeList.add(QlockLanguage.Y);
                timeList.add(QlockLanguage.VEINTE);
                break;
            case 25:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                timeList.add(QlockLanguage.Y);
                timeList.add(QlockLanguage.VEINTICINCO);
                break;
            case 30:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                timeList.add(QlockLanguage.Y);
                timeList.add(QlockLanguage.MEDIA);
                break;
            case 35:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.MENOS);
                timeList.add(QlockLanguage.VEINTICINCO);
                addHour(timeList, hour);
                break;
            case 40:
                timeList.add(hour == 1 ? QlockLanguage.ES : QlockLanguage.SON);
                timeList.add(hour == 1 ? QlockLanguage.LA : QlockLanguage.LAS);
                timeList.add(QlockLanguage.MENOS);
                timeList.add(QlockLanguage.VEINTE);
                addHour(timeList, hour);
                break;
            case 45:
                timeList.add(QlockLanguage.SON);
                timeList.add(QlockLanguage.LAS);
                timeList.add(QlockLanguage.MENOS);
                timeList.add(QlockLanguage.CUARTO);
                addHour(timeList, hour);
                break;
            case 50:
                // ES LA UNA MENOS DIEZ
                timeList.add(QlockLanguage.SON);
                timeList.add(QlockLanguage.LAS);
                timeList.add(QlockLanguage.MENOS);
                timeList.add(QlockLanguage.DIEZ1);
                addHour(timeList, hour);
                break;
            case 55:
                timeList.add(QlockLanguage.SON);
                timeList.add(QlockLanguage.LAS);
                timeList.add(QlockLanguage.MENOS);
                timeList.add(QlockLanguage.CINCO1);
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
            timeList.add(QlockLanguage.UNA);
        } else if (HOUR == 10) {
            timeList.add(QlockLanguage.DIEZ1);
        } else {
            timeList.add(QlockLanguage.valueOf(LOOKUP.get(HOUR + 1)));
        }
    }

    private enum QlockLanguage implements QlockWord {
        UNA(0, 8, 10),
        DOS(1, 0, 2),
        TRES(1, 4, 7),
        CUATRO(2, 0, 5),
        CINCO(2, 6, 10),
        CINCO1(8, 6, 10),
        SEIS(3, 0, 3),
        SIETE(3, 5, 9),
        OCHO(4, 0, 3),
        NUEVE(4, 4, 8),
        DIEZ(5, 2, 5),
        DIEZ1(7, 7, 10),
        ONCE(7, 5, 10),
        DOCE(6, 0, 3),
        SON(0, 1, 3),
        ES(0, 0, 1),
        LA(0, 5, 6),
        LAS(0, 5, 7),
        Y(6, 5, 5),
        MENOS(6, 6, 10),
        CUARTO(9, 5, 10),
        VEINTE(7, 1, 6),
        VEINTICINCO(8, 0, 10),
        MEDIA(9, 0, 4);

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
