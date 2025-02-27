/**
 * Request provides coordinate information but is incomplete.
 */
class MissingCoordinateException extends Error{
    constructor(message: string) {
        super(message);
        this.name = "MissingCoordinateException"; 
    }
}

export default MissingCoordinateException;
