/*
 * Copyright 2017-2021 Dromara.org
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

package org.dromara.hmily.tac.sqlparser.model.common.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.hmily.tac.sqlparser.model.common.constant.HmilyParen;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyDMLStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyDeleteStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyUpdateStatement;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * SQL utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HmilySQLUtil {
    
    /**
     * Get exactly number value and type.
     *
     * @param value string to be converted
     * @param radix radix
     * @return exactly number value and type
     */
    public static Number getExactlyNumber(final String value, final int radix) {
        try {
            return getBigInteger(value, radix);
        } catch (final NumberFormatException ex) {
            return new BigDecimal(value);
        }
    }
    
    private static Number getBigInteger(final String value, final int radix) {
        BigInteger result = new BigInteger(value, radix);
        if (result.compareTo(new BigInteger(String.valueOf(Integer.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Integer.MAX_VALUE))) <= 0) {
            return result.intValue();
        }
        if (result.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) >= 0 && result.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) <= 0) {
            return result.longValue();
        }
        return result;
    }
    
    /**
     * Get exactly value for SQL expression.
     * 
     * <p>remove special char for SQL expression</p>
     * 
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExactlyValue(final String value) {
        return null == value ? null : CharMatcher.anyOf("[]`'\"").removeFrom(value);
    }
    
    /**
     * Get exactly SQL expression.
     *
     * <p>remove space for SQL expression</p>
     *
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExactlyExpression(final String value) {
        return Strings.isNullOrEmpty(value) ? value : CharMatcher.anyOf(" ").removeFrom(value);
    }
    
    /**
     * Get exactly SQL expression without outside parentheses.
     * 
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExpressionWithoutOutsideParentheses(final String value) {
        int parenthesesOffset = getParenthesesOffset(value);
        return 0 == parenthesesOffset ? value : value.substring(parenthesesOffset, value.length() - parenthesesOffset);
    }
    
    private static int getParenthesesOffset(final String value) {
        int result = 0;
        if (Strings.isNullOrEmpty(value)) {
            return result;
        }
        while (HmilyParen.PARENTHESES.getLeftParen() == value.charAt(result)) {
            result++;
        }
        return result;
    }
    
    /**
     * Determine whether SQL is read-only.
     *
     * @param sqlStatement SQL statement
     * @return true if read-only, otherwise false
     */
    public static boolean isReadOnly(final HmilyStatement sqlStatement) {
        if (sqlStatement instanceof HmilyDMLStatement) {
            return isReadOnly((HmilyDMLStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }
    
    private static boolean isReadOnly(final HmilyDMLStatement sqlStatement) {

        if (sqlStatement instanceof HmilyUpdateStatement || sqlStatement instanceof HmilyDeleteStatement || sqlStatement instanceof HmilyInsertStatement) {
            return false;
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }
}
