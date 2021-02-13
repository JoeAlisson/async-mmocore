/*
 * Copyright © 2019-2021 Async-mmocore
 *
 * This file is part of the Async-mmocore project.
 *
 * Async-mmocore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Async-mmocore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.joealisson.mmocore.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public class Comparator {


    public static void assertEquals(long expected, long actual) throws ValueMismatchException {
        if(expected != actual) {
            mismatch(expected, actual);
        }
    }

    public static void assertEquals(double expected, double actual) throws ValueMismatchException {
        if(Double.compare(expected, actual) != 0) {
            mismatch(expected, actual);
        }
    }

    public static void assertEquals(String expected, String actual) throws ValueMismatchException {
        if(isNull(expected) && nonNull(actual) || !expected.equals(actual)) {
            mismatch(expected, actual);
        }
    }

    private static void mismatch(Object expected, Object actual) throws ValueMismatchException {
        throw new ValueMismatchException(expected.toString(), actual.toString());
    }
}
