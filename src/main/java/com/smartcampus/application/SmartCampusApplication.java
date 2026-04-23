package com.smartcampus.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.exception.RoomNotEmptyExceptionMapper;
import com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.exception.SensorUnavailableExceptionMapper;
import com.smartcampus.exception.ResourceNotFoundExceptionMapper;
import com.smartcampus.exception.GlobalExceptionMapper;
import com.smartcampus.filter.LoggingFilter;

@ApplicationPath("/")
public class SmartCampusApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(ResourceNotFoundExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(LoggingFilter.class);
        return classes;
    }
}
