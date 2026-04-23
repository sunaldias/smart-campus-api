package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import com.smartcampus.util.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

        @GET
        public Response getAllSensors(@QueryParam("type") String type) {
                List<Sensor> sensors = new ArrayList<>(DataStore.sensors.values());

                if (type != null && !type.trim().isEmpty()) {
                        String requestedType = type.trim().toLowerCase(Locale.ROOT);
                        sensors = sensors.stream()
                                        .filter(sensor -> sensor.getType() != null
                                                        && sensor.getType().trim().toLowerCase(Locale.ROOT)
                                                                        .equals(requestedType))
                                        .collect(Collectors.toList());
                }

                return Response.ok(sensors).build();
        }

        @POST
        public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
                if (sensor == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new ErrorResponse(400, "Bad Request", "Sensor body is required"))
                                        .build();
                }

                if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new ErrorResponse(400, "Bad Request", "Sensor id is required"))
                                        .build();
                }

                if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new ErrorResponse(400, "Bad Request", "Sensor type is required"))
                                        .build();
                }

                if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new ErrorResponse(400, "Bad Request", "Sensor status is required"))
                                        .build();
                }

                if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new ErrorResponse(400, "Bad Request", "roomId is required"))
                                        .build();
                }

                if (DataStore.sensors.containsKey(sensor.getId())) {
                        return Response.status(Response.Status.CONFLICT)
                                        .entity(new ErrorResponse(409, "Conflict",
                                                        "A sensor with this id already exists"))
                                        .build();
                }

                Room room = DataStore.rooms.get(sensor.getRoomId());
                if (room == null) {
                        throw new LinkedResourceNotFoundException(
                                        "Cannot create sensor because the referenced room does not exist: "
                                                        + sensor.getRoomId());
                }

                DataStore.sensors.put(sensor.getId(), sensor);
                room.getSensorIds().add(sensor.getId());

                URI uri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
                return Response.created(uri).entity(sensor).build();
        }

        @Path("/{sensorId}/readings")
        public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
                return new SensorReadingResource(sensorId);
        }
}