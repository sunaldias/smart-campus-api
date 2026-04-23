package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Map<String, Object> getApiInfo(@Context UriInfo uriInfo) {
        String base = uriInfo.getBaseUri().toString();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apiName", "Smart Campus API");
        response.put("version", "v1");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("adminContact", "w2151897@westminster.ac.uk");

        // HATEOAS links
        List<Map<String, String>> links = new ArrayList<>();

        Map<String, String> self = new LinkedHashMap<>();
        self.put("rel", "self");
        self.put("href", base);
        self.put("method", "GET");
        links.add(self);

        Map<String, String> rooms = new LinkedHashMap<>();
        rooms.put("rel", "rooms");
        rooms.put("href", base + "rooms");
        rooms.put("method", "GET");
        links.add(rooms);

        Map<String, String> sensors = new LinkedHashMap<>();
        sensors.put("rel", "sensors");
        sensors.put("href", base + "sensors");
        sensors.put("method", "GET");
        links.add(sensors);

        response.put("links", links);

        return response;
    }
}