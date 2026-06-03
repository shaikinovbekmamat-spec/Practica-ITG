/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.web;

import com.axelor.app.AppSettings;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@jakarta.ws.rs.Path("/custom-file")
public class FileViewController {

  @GET
  @jakarta.ws.rs.Path("/view/{metaFileId}")
  public Response viewImage(@PathParam("metaFileId") Long metaFileId) {
    try {

      MetaFile metaFile = Beans.get(MetaFileRepository.class).find(metaFileId);
      if (metaFile == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String uploadDir = AppSettings.get().get("data.upload.dir");
      java.nio.file.Path filePath = Paths.get(uploadDir, metaFile.getFilePath());

      if (!Files.exists(filePath)) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String contentType = Optional.ofNullable(metaFile.getFileType()).orElse("image/jpeg");

      return Response.ok(filePath.toFile())
          .type(contentType)
          .header("Content-Disposition", "inline; filename=\"" + metaFile.getFileName() + "\"")
          .build();

    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
