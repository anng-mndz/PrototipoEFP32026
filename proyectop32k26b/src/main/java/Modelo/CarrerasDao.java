package Modelo;

import Controlador.clsCarreras;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Modelo - CarrerasDAO
 * Gestiona todas las operaciones CRUD sobre la tabla 'carreras'
 * y registra cada movimiento en la tabla 'bitacora_seguridad'.
 * Patrón: Model-View-Controller (MVC)
 * Capa  : Data Access Object (DAO / Controlador de datos)
 * Luis Angel Méndez Fuentes
 * 9959-24-6845
 */
public class CarrerasDao {

    // ─── Cadena de conexión ──────────────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/sig";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "";          

    // ─── Obtener conexión ────────────────────────────────────────────────────
    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BITÁCORA DE SEGURIDAD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Registra un movimiento en la bitácora de seguridad.
     *
     * @param operacion  Tipo de operación: INSERT, UPDATE, DELETE, SELECT
     * @param detalle    Descripción del movimiento realizado
     * @param usuario    Usuario que realizó la operación
     */
    public void registrarBitacora(String operacion, String detalle, String usuario) {
        String sql = "INSERT INTO bitacora_seguridad "
                   + "(operacion, detalle, usuario, fecha_hora) "
                   + "VALUES (?, ?, ?, NOW())";
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, operacion);
            ps.setString(2, detalle);
            ps.setString(3, usuario);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al registrar en bitácora: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CREATE – Insertar carrera
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Inserta un nuevo registro en la tabla carreras.
     *
     * @param carrera  Objeto clsCarreras con los datos a insertar
     * @param usuario  Usuario que realiza la operación
     * @return true si la inserción fue exitosa, false en caso contrario
     */
    public boolean insertar(clsCarreras carrera, String usuario) {
        String sql = "INSERT INTO carreras "
                   + "(codigo_carrera, nombre_carrera, codigo_facultad, estatus_carrera) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, carrera.getCodigoCarrera());
            ps.setString(2, carrera.getNombreCarrera());
            ps.setString(3, carrera.getCodigoFacultad());
            ps.setString(4, carrera.getEstatusCarrera());
            ps.executeUpdate();

            // Registrar en bitácora
            registrarBitacora("INSERT",
                "Alta de carrera: código=" + carrera.getCodigoCarrera()
                + ", nombre=" + carrera.getNombreCarrera(), usuario);

            return true;

        } catch (SQLException e) {
            System.err.println("Error al insertar carrera: " + e.getMessage());
            return false;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  READ – Consultar carrera por código
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Consulta una carrera por su código primario.
     *
     * @param codigoCarrera  Código de la carrera a buscar
     * @param usuario        Usuario que realiza la consulta
     * @return Objeto clsCarreras si existe, null si no se encontró
     */
    public clsCarreras consultar(String codigoCarrera, String usuario) {
        String sql = "SELECT codigo_carrera, nombre_carrera, "
                   + "codigo_facultad, estatus_carrera "
                   + "FROM carreras WHERE codigo_carrera = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoCarrera);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                clsCarreras c = new clsCarreras(
                    rs.getString("codigo_carrera"),
                    rs.getString("nombre_carrera"),
                    rs.getString("codigo_facultad"),
                    rs.getString("estatus_carrera")
                );
                registrarBitacora("SELECT",
                    "Consulta de carrera: código=" + codigoCarrera, usuario);
                return c;
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar carrera: " + e.getMessage());
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  READ – Listar todas las carreras
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retorna la lista completa de carreras registradas.
     *
     * @return Lista de objetos clsCarreras
     */
    public List<clsCarreras> listarTodos() {
        List<clsCarreras> lista = new ArrayList<>();
        String sql = "SELECT codigo_carrera, nombre_carrera, "
                   + "codigo_facultad, estatus_carrera "
                   + "FROM carreras ORDER BY codigo_carrera";
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new clsCarreras(
                    rs.getString("codigo_carrera"),
                    rs.getString("nombre_carrera"),
                    rs.getString("codigo_facultad"),
                    rs.getString("estatus_carrera")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar carreras: " + e.getMessage());
        }
        return lista;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPDATE – Modificar carrera
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Modifica los datos de una carrera existente.
     *
     * @param carrera  Objeto clsCarreras con los nuevos datos (el código NO cambia)
     * @param usuario  Usuario que realiza la modificación
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean modificar(clsCarreras carrera, String usuario) {
        String sql = "UPDATE carreras SET nombre_carrera = ?, "
                   + "codigo_facultad = ?, estatus_carrera = ? "
                   + "WHERE codigo_carrera = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, carrera.getNombreCarrera());
            ps.setString(2, carrera.getCodigoFacultad());
            ps.setString(3, carrera.getEstatusCarrera());
            ps.setString(4, carrera.getCodigoCarrera());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                registrarBitacora("UPDATE",
                    "Modificación de carrera: código=" + carrera.getCodigoCarrera()
                    + ", nuevo nombre=" + carrera.getNombreCarrera(), usuario);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al modificar carrera: " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DELETE – Eliminar carrera
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Elimina una carrera de la base de datos por su código.
     *
     * @param codigoCarrera  Código de la carrera a eliminar
     * @param usuario        Usuario que realiza la eliminación
     * @return true si la eliminación fue exitosa, false en caso contrario
     */
    public boolean eliminar(String codigoCarrera, String usuario) {
        String sql = "DELETE FROM carreras WHERE codigo_carrera = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoCarrera);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                registrarBitacora("DELETE",
                    "Baja de carrera: código=" + codigoCarrera, usuario);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al eliminar carrera: " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Consultar bitácora completa
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retorna todos los registros de la bitácora de seguridad.
     *
     * @return ResultSet con todos los movimientos registrados
     *         (el llamador es responsable de cerrar la conexión)
     */
    public List<String[]> consultarBitacora() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_bitacora, operacion, detalle, usuario, fecha_hora "
                   + "FROM bitacora_seguridad ORDER BY fecha_hora DESC";
        try (Connection conn = obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id_bitacora")),
                    rs.getString("operacion"),
                    rs.getString("detalle"),
                    rs.getString("usuario"),
                    rs.getString("fecha_hora")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar bitácora: " + e.getMessage());
        }
        return lista;
    }
}