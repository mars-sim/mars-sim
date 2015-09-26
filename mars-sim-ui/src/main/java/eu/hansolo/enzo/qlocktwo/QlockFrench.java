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
public class QlockFrench implements Qlock {
    private static final QlockTwo.Language LANGUAGE = QlockTwo.Language.FRENCH;
    private static final String[][] MATRIX = {
        {"I", "L", "N", "E", "S", "T", "O", "D", "E", "U", "X"},
        {"Q", "U", "A", "T", "R", "E", "T", "R", "O", "I", "S"},
        {"N", "E", "U", "F", "U", "N", "E", "S", "E", "P", "T"},
        {"H", "U", "I", "T", "S", "I", "X", "C", "I", "N", "Q"},
        {"M", "I", "D", "I", "X", "M", "I", "N", "U", "I", "T"},
        {"O", "N", "Z", "E", "R", "H", "E", "U", "R", "E", "S"},
        {"M", "O", "I", "N", "S", "O", "L", "E", "D", "I", "X"},
        {"E", "T", "R", "Q", "U", "A", "R", "T", "P", "M", "D"},
        {"V", "I", "N", "G", "T", "-", "C", "I", "N", "Q", "U"},
        {"E", "T", "S", "D", "E", "M", "I", "E", "P", "A", "M"}
    };
    private final ConcurrentHashMap<Integer, String> LOOKUP;
    private List<QlockWord> timeList;


    public QlockFrench() {
        LOOKUP = new ConcurrentHashMap<>();
        LOOKUP.putAll(QlockTwo.Language.FRENCH.getLookup());
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

        timeList.add(QlockLanguage.IL);
        timeList.add(QlockLanguage.EST);
        switch (minute) {
            case 0:
                if (hour == 12) {
                    timeList.add(QlockLanguage.MIDI);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                break;
            case 5:
                if (hour == 12) {
                    timeList.add(QlockLanguage.MIDI);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.CINQ1);
                break;
            case 10:
                if (hour == 12) {
                    timeList.add(QlockLanguage.MIDI);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.DIX1);
                break;
            case 15:
                if (hour == 12) {
                    timeList.add(QlockLanguage.MIDI);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.ET);
                timeList.add(QlockLanguage.QUART);
                break;
            case 20:
                if (hour == 12) {
                    timeList.add(QlockLanguage.MIDI);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.VINGT);
                break;
            case 25:
                if (hour == 12) {
                    timeList.add(QlockLanguage.MIDI);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.VINGT_CINQ);
                break;
            case 30:
                if (hour == 12) {
                    timeList.add(QlockLanguage.MIDI);
                } else {
                    timeList.add(QlockLanguage.valueOf(LOOKUP.get(hour)));
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.ET1);
                timeList.add(QlockLanguage.DEMIE);
                break;
            case 35:
                addHour(timeList, hour);
                if (hour != 11) {
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.MOINS);
                timeList.add(QlockLanguage.VINGT_CINQ);
                break;
            case 40:
                addHour(timeList, hour);
                if (hour != 11) {
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.MOINS);
                timeList.add(QlockLanguage.VINGT);
                break;
            case 45:
                addHour(timeList, hour);
                if (hour != 11) {
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.MOINS);
                timeList.add(QlockLanguage.LE);
                timeList.add(QlockLanguage.QUART);
                break;
            case 50:
                addHour(timeList, hour);
                if (hour != 11) {
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.MOINS);
                timeList.add(QlockLanguage.DIX1);
                break;
            case 55:
                addHour(timeList, hour);
                if (hour != 11) {
                    timeList.add(hour == 1 ? QlockLanguage.HEURE : QlockLanguage.HEURES);
                }
                timeList.add(QlockLanguage.MOINS);
                timeList.add(QlockLanguage.CINQ1);
                break;
        }

        return timeList;
    }

    @Override public QlockTwo.Language getLanguage() {
        return LANGUAGE;
    }

    private void addHour(List<QlockWord> timeList, final int HOUR) {
        if (HOUR == 12) {
            timeList.add(QlockLanguage.UNE);
        } else if (HOUR == 5) {
            timeList.add(QlockLanguage.CINQ1);
        } else if (HOUR == 11) {
            timeList.add(QlockLanguage.MIDI);
        } else {
            timeList.add(QlockLanguage.valueOf(LOOKUP.get(HOUR + 1)));
        }
    }

    private enum QlockLanguage implements QlockWord {
        UNE(2, 4, 6),
        DEUX(0, 7, 10),
        TROIS(1, 6, 10),
        QUATRE(1, 0, 5),
        CINQ(3, 7, 10),
        CINQ1(8, 6, 9),
        SIX(3, 4, 6),
        SEPT(2, 7, 10),
        HUIT(3, 0, 3),
        NEUF(2, 0, 3),
        DIX(4, 2, 4),
        DIX1(6, 8, 10),
        ONZE(5, 0, 3),
        IL(0, 0, 1),
        EST(0, 3, 5),
        ET(7, 0, 1),
        HEURE(5, 5, 9),
        HEURES(5, 5, 10),
        LE(6, 6, 7),
        QUART(7, 3, 7),
        VINGT(8, 0, 4),
        MIDI(4, 0, 3),
        VINGT_CINQ(8, 0, 9),
        MOINS(6, 0, 4),
        ET1(9, 0, 1),
        DEMI(9, 3, 6),
        DEMIE(9, 3, 7);

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
