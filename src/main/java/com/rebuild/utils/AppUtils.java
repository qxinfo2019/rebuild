/*
rebuild - Building your system freely.
Copyright (C) 2018 devezhao <zhaofang123@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package com.rebuild.utils;

import java.nio.file.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.rebuild.api.Controll;
import com.rebuild.server.Application;
import com.rebuild.web.admin.AdminEntryControll;

import cn.devezhao.commons.ThrowableUtils;
import cn.devezhao.commons.web.ServletUtils;
import cn.devezhao.commons.web.WebUtils;
import cn.devezhao.persist4j.engine.ID;

/**
 * 封裝一些有用的工具方法
 * 
 * @author zhaofang123@gmail.com
 * @since 05/19/2018
 */
public class AppUtils {
	
	/**
	 * @return
	 */
	public static boolean devMode() {
		return Application.devMode();
	}

	/**
	 * 获取当前请求用户
	 * 
	 * @param request
	 * @return null or UserID
	 */
	public static ID getRequestUser(HttpServletRequest request) {
		Object user = request.getSession(true).getAttribute(WebUtils.CURRENT_USER);
		return user == null ? null : (ID) user;
	}
	
	/**
	 * @param request
	 * @return
	 */
	public static boolean isAdminUser(HttpServletRequest request) {
		ID uid = getRequestUser(request);
		if (uid == null) {
			return false;
		}
		return Application.getUserStore().getUser(uid).isAdmin();
	}
	
	/**
	 * @param request
	 * @return
	 */
	public static boolean isAdminVerified(HttpServletRequest request) {
		return AdminEntryControll.isAdminVerified(request);
	}
	
	/**
	 * 格式化标准的客户端消息
	 * 
	 * @param errorCode
	 * @param errorMsg
	 * @return
	 * @see Controll
	 */
	public static String formatControllMsg(int errorCode, String errorMsg) {
		JSONObject map = new JSONObject();
		map.put("error_code", errorCode);
		if (errorMsg != null) {
			if (errorCode == 0) {
				map.put("data", errorMsg);
			} else {
				map.put("error_msg", errorMsg);
			}
		}
		return map.toJSONString();
	}
	
	/**
	 * 获取后台抛出的错误消息
	 * 
	 * @param request
	 * @param exception
	 * @return
	 */
	public static String getErrorMessage(HttpServletRequest request, Throwable exception) {
		String errorMsg = (String) request.getAttribute(ServletUtils.ERROR_MESSAGE);
		if (StringUtils.isNotBlank(errorMsg)) {
			return errorMsg;
		}
		
		Throwable ex = (Throwable) request.getAttribute(ServletUtils.ERROR_EXCEPTION);
		if (ex == null) {
			ex = (Throwable) request.getAttribute(ServletUtils.JSP_JSP_EXCEPTION);
		}
		if (ex == null && exception != null) {
			ex = exception;
		}
		if (ex != null) {
			ex = ThrowableUtils.getRootCause(ex);
		}
		
		if (ex == null) {
			Integer state = (Integer) request.getAttribute(ServletUtils.ERROR_STATUS_CODE);
			if (state != null && state == 404) {
				return "访问的地址/资源不存在";
			} else {
				return "系统繁忙，请稍后重试";
			}
		} else if (ex instanceof AccessDeniedException) {
			return "权限不足，访问被阻止";
		}
		
		errorMsg = StringUtils.defaultIfBlank(ex.getLocalizedMessage(), "未知系统错误");
		return ex.getClass().getSimpleName() + " : " + errorMsg;
	}
}
