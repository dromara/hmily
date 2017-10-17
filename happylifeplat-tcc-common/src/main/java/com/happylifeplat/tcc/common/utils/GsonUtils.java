/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.happylifeplat.tcc.common.utils;

import com.google.gson.Gson;

/**
 * @author xiaoyu
 */
public class GsonUtils {

    private static final GsonUtils GSON_UTILS = new GsonUtils();

    private static final Gson GSON = new Gson();

    public static GsonUtils getInstance() {
        return GSON_UTILS;
    }

    public String toJson(Object object) {
        return GSON.toJson(object);
    }

    public <T> T fromJson(String json, Class<T> tClass) {
        return GSON.fromJson(json, tClass);
    }
}
