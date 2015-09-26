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
public class QlockGerman implements Qlock {
    private static final QlockTwo.Language LANGUAGE = QlockTwo.Language.GERMAN;
    private static final String[][] MATRIX = {
        {"E", "S", "K", "I", "S", "T", "A", "F", "Ü", "N", "F"},
        {"Z", "E", "H", "N", "Z", "W", "A", "N", "Z", "I", "G"},
        {"D", "R", "E", "I", "V", "I", "E", "R", "T", "E", "L"},
        {"V", "O", "R", "F", "U", "N", "K", "N", "A", "C", "H"},
        {"H", "A", "L", "B", "A", "E", "L", "F", "Ü", "N", "F"},
        {"E", "I", "N", "S", "X", "Ä", "M", "Z", "W", "E", "I"},
        {"D", "R", "E", "I", "A", "U", "I", "V", "I", "E", "R"},
        {"S", "E", "C", "H", "S", "N", "L", "A", "C", "H", "T"},
        {"S", "I", "E", "B", "E", "N", "Z", "W", "Ö", "L", "F"},
        {"Z", "E", "H", "N", "E", "U", "N", "K", "U", "H", "R"}
    };
    private final ConcurrentHashMap<Integer, String> LOOKUP;
    private List<QlockWord> timeList;


    public QlockGerman() {
        LOOKUP = new ConcurrentHashMap<>();
        LOOKUP.putAll(QlockTwo.Language.GERMAN.getLookup());
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

        timeList.add(QlockLanguage.ES);
        timeList.add(QlockLanguage.IST);
        switch (minute) {
            case 0:
                if (10 == hour) {
                    timeList.add(QlockLanguage.ZEHN1);
                } else if (1 == hour) {
                    timeList.add(QlockLanguage.EIN);
                } else if (12 == hour) {
                    timeList.add(QlockLanguage.ZWOELF);
                } else if (5 == hour) {
                    timeList.add(QlockLanguage.FUENF);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                }
                timeList.add(QlockLanguage.UHR);
                break;
            case 5:
                timeList.add(QlockLanguage.FUENF1);
                timeList.add(QlockLanguage.NACH);
                if (10 == hour) {
                    timeList.add(QlockLanguage.ZEHN1);
                } else if (3 == hour) {
                    timeList.add(QlockLanguage.DREI1);
                } else if (5 == hour) {
                    timeList.add(QlockLanguage.FUENF);
                } else if (12 == hour) {
                    timeList.add(QlockLanguage.ZWOELF);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                }
                break;
            case 10:
                timeList.add(QlockLanguage.ZEHN);
                timeList.add(QlockLanguage.NACH);
                if (10 == hour) {
                    timeList.add(QlockLanguage.ZEHN1);
                } else if (3 == hour) {
                    timeList.add(QlockLanguage.DREI1);
                } else if (5 == hour) {
                    timeList.add(QlockLanguage.FUENF);
                } else if (12 == hour) {
                    timeList.add(QlockLanguage.ZWOELF);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                }
                break;
            case 15:
                timeList.add(QlockLanguage.VIERTEL);
                timeList.add(QlockLanguage.NACH);
                if (10 == hour) {
                    timeList.add(QlockLanguage.ZEHN1);
                } else if (3 == hour) {
                    timeList.add(QlockLanguage.DREI1);
                } else if (5 == hour) {
                    timeList.add(QlockLanguage.FUENF);
                } else if (12 == hour) {
                    timeList.add(QlockLanguage.ZWOELF);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                }
                break;
            case 20:
                timeList.add(QlockLanguage.ZWANZIG);
                timeList.add(QlockLanguage.NACH);
                if (10 == hour) {
                    timeList.add(QlockLanguage.ZEHN1);
                } else if (3 == hour) {
                    timeList.add(QlockLanguage.DREI1);
                } else if (5 == hour) {
                    timeList.add(QlockLanguage.FUENF);
                } else if (12 == hour) {
                    timeList.add(QlockLanguage.ZWOELF);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                }
                break;
            case 25:
                timeList.add(QlockLanguage.FUENF1);
                timeList.add(QlockLanguage.VOR);
                if (9 == hour) {
                    timeList.add(QlockLanguage.ZEHN1);
                } else if (2 == hour) {
                    timeList.add(QlockLanguage.DREI1);
                } else if (4 == hour) {
                    timeList.add(QlockLanguage.FUENF);
                } else if (11 == hour) {
                    timeList.add(QlockLanguage.ZWOELF);
                }
                timeList.add(QlockLanguage.HALB);
                addHour(timeList, hour);
                break;
            case 30:
                timeList.add(QlockLanguage.HALB);
                addHour(timeList, hour);
                break;
            case 35:
                timeList.add(QlockLanguage.FUENF1);
                timeList.add(QlockLanguage.NACH);
                timeList.add(QlockLanguage.HALB);
                addHour(timeList, hour);
                break;
            case 40:
                timeList.add(QlockLanguage.ZWANZIG);
                timeList.add(QlockLanguage.VOR);
                addHour(timeList, hour);
                break;
            case 45:
                timeList.add(QlockLanguage.VIERTEL);
                timeList.add(QlockLanguage.VOR);
                addHour(timeList, hour);
                break;
            case 50:
                timeList.add(QlockLanguage.ZEHN);
                timeList.add(QlockLanguage.VOR);
                addHour(timeList, hour);
                break;
            case 55:
                timeList.add(QlockLanguage.FUENF1);
                timeList.add(QlockLanguage.VOR);
                if (12 == hour) {
                    timeList.add(QlockLanguage.ZWOELF);
                } else {
                    addHour(timeList, hour);
                }
                break;
        }
        return timeList;
    }

    @Override public QlockTwo.Language getLanguage() {
        return LANGUAGE;
    }

    private void addHour(List<QlockWord> timeList, final int HOUR) {
        if (HOUR == 12) {
            timeList.add(QlockLanguage.EINS);
        } else {
            if (HOUR + 1 == 5) {
                timeList.add(QlockLanguage.FUENF2);
            } else if (HOUR + 1 == 10) {
                timeList.add(QlockLanguage.ZEHN1);
            } else if (HOUR + 1 == 3) {
                timeList.add(QlockLanguage.DREI1);
            } else if (HOUR + 1 == 12) {
                timeList.add(QlockLanguage.ZWOELF);
            } else {
                timeList.add(QlockLanguage.valueOf(LOOKUP.get(HOUR + 1)));
            }
        }
    }

    private enum QlockLanguage implements QlockWord {
        EIN(5, 0, 2),
        EINS(5, 0, 3),
        ZWEI(5, 7, 10),
        DREI(2, 0, 3),
        DREI1(6, 0, 3),
        VIER(6, 7, 10),
        FUENF(4, 7, 10),
        FUENF1(0, 7, 10),
        FUENF2(4, 7, 10),
        SECHS(7, 0, 4),
        SIEBEN(8, 0, 5),
        ACHT(7, 7, 10),
        NEUN(9, 3, 6),
        ZEHN(1, 0, 3),
        ZEHN1(9, 0, 3),
        ELF(4, 5, 7),
        ZWOELF(8, 6, 10),
        ES(0, 0, 1),
        IST(0, 3, 5),
        VOR(3, 0, 2),
        NACH(3, 7, 10),
        VIERTEL(2, 4, 10),
        DREIVIERTEL(2, 0, 10),
        HALB(4, 0, 3),
        ZWANZIG(1, 4, 10),
        UHR(9, 8, 10);

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
