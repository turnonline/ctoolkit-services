package org.ctoolkit.services.storage;

/**
 * Data upload handler constants.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface DataUploadHandler
{
    /**
     * The URL of the servlet to register in order to handle data upload.
     */
    String DATA_HANDLER_UPLOAD_URL = "/data-upload-handler";

    /**
     * The mandatory file marker.
     */
    String UPLOAD_NAME_FIELD_MARKER = "__name_field_marker";

    /**
     * Google cloud storage bucket name, if not used the default GCS bucket name (AppIdentity) will be used.
     */
    String PARAMETER_GCS_BUCKET_NAME = "__gcs_bucket_name";

    /**
     * The optional arbitrary custom name
     */
    String PARAMETER_CUSTOM_NAME = "__arb_custom_name";

    /**
     * The optional size of the image returned as serving URL. Valid sizes must be between 0 - 1600
     */
    String PARAMETER_IMAGE_SIZE = "__image_size";
}
