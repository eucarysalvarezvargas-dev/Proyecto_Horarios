package com.horarios.controller;

import com.horarios.model.Docente;
import com.horarios.model.Usuario;
import com.horarios.model.dao.UsuarioDAO;
import com.horarios.util.ControladorBase;
import com.horarios.util.PasswordUtil;
import com.horarios.util.PermisosRol;
import com.horarios.util.PersistenciaConfig;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioController extends ControladorBase {

    private final List<Usuario> usuarios;
    private int secuenciaId;
    private final UsuarioDAO usuarioDAO;
    private final boolean jdbc;
    private DocenteController docenteController;
    private boolean omitirVinculo;

    public UsuarioController() {
        this.jdbc = PersistenciaConfig.isJdbcDisponible();
        this.usuarioDAO = jdbc ? new UsuarioDAO() : null;
        this.usuarios = jdbc ? null : new ArrayList<>();
        this.secuenciaId = 1;
        if (!jdbc) {
            cargarAdminPorDefecto();
        } else {
            migrarClavesTextoPlano();
        }
    }

    /** Convierte contraseñas legadas en texto plano a hash SHA-256 + salt. */
    private void migrarClavesTextoPlano() {
        try {
            for (Usuario u : usuarioDAO.listarTodos()) {
                if (u.getPassword() != null && !PasswordUtil.esAlmacenadoHasheado(u.getPassword())) {
                    Usuario m = copiarUsuario(u);
                    m.setPassword(PasswordUtil.hashPassword(u.getPassword()));
                    usuarioDAO.actualizar(m);
                }
            }
        } catch (SQLException ex) {
            // Si falla la migración, el login seguirá rechazando texto plano tras el cambio de PasswordUtil.
        }
    }

    private static Usuario copiarUsuario(Usuario u) {
        Usuario m = new Usuario();
        m.setIdUsuario(u.getIdUsuario());
        m.setUsername(u.getUsername());
        m.setPassword(u.getPassword());
        m.setRol(u.getRol());
        m.setCedula(u.getCedula());
        m.setNombres(u.getNombres());
        m.setApellidos(u.getApellidos());
        m.setEstado(u.getEstado());
        return m;
    }

    public void setDocenteController(DocenteController docenteController) {
        this.docenteController = docenteController;
    }

    public boolean actualizarSinVinculo(Usuario usuario) {
        boolean prev = omitirVinculo;
        omitirVinculo = true;
        try {
            return actualizar(usuario);
        } finally {
            omitirVinculo = prev;
        }
    }

    public boolean eliminarLogicoSinVinculo(int idUsuario) {
        boolean prev = omitirVinculo;
        omitirVinculo = true;
        try {
            return eliminarLogico(idUsuario);
        } finally {
            omitirVinculo = prev;
        }
    }

    public boolean reactivarComoDocente(Usuario inactivo, Docente docente) {
        if (inactivo == null || docente == null) {
            return false;
        }
        try {
            if (jdbc && !usuarioDAO.reactivar(inactivo.getIdUsuario())) {
                setUltimoError("No se pudo reactivar el usuario.");
                return false;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return false;
        }
        Usuario act = copiarUsuario(inactivo);
        act.setEstado(1);
        act.setRol("DOCENTE");
        act.setUsername(docente.getCedula());
        act.setCedula(docente.getCedula());
        act.setNombres(docente.getNombres());
        act.setApellidos(docente.getApellidos());
        return actualizarSinVinculo(act);
    }

    private static String resolverCedulaVinculo(Usuario u) {
        if (u == null) {
            return null;
        }
        if (u.getCedula() != null && !u.getCedula().trim().isEmpty()) {
            return u.getCedula().trim();
        }
        if (u.getUsername() != null && u.getUsername().trim().matches("^\\d{8}$")) {
            return u.getUsername().trim();
        }
        return null;
    }

    private static boolean esRolDocente(String rol) {
        return rol != null && "DOCENTE".equalsIgnoreCase(rol.trim());
    }

    private boolean validarReglasVinculoAlta(Usuario usuario) {
        if (esRolDocente(usuario.getRol())) {
            setUltimoError("El rol DOCENTE no se asigna desde Usuarios.\n\nUse el módulo Docentes.");
            return false;
        }
        return true;
    }

    private boolean validarReglasVinculoCambio(Usuario anterior, Usuario nuevo) {
        if (omitirVinculo || docenteController == null) {
            return true;
        }
        boolean eraDocente = esRolDocente(anterior.getRol());
        boolean esDocente = esRolDocente(nuevo.getRol());
        String cedAnterior = resolverCedulaVinculo(anterior);
        String cedNueva = resolverCedulaVinculo(nuevo);

        if (eraDocente && !esDocente) {
            if (cedAnterior != null) {
                Docente d = docenteController.buscarPorCedula(cedAnterior);
                if (d != null && docenteController.tieneHorariosActivos(d.getIdDocente())) {
                    setUltimoError("No puede cambiar el rol: el docente tiene horarios activos asignados.");
                    return false;
                }
            }
            return true;
        }
        if (!eraDocente && esDocente) {
            setUltimoError("El rol DOCENTE solo se asigna desde el módulo Docentes.");
            return false;
        }
        if (esDocente && cedNueva != null && docenteController.buscarPorCedula(cedNueva) == null) {
            setUltimoError("El docente con cédula " + cedNueva + " no está activo en el catálogo.");
            return false;
        }
        return true;
    }

    private void aplicarVinculoTrasPersistir(Usuario anterior, Usuario nuevo) {
        if (omitirVinculo || docenteController == null || anterior == null || nuevo == null) {
            return;
        }
        boolean eraDocente = esRolDocente(anterior.getRol());
        boolean esDocente = esRolDocente(nuevo.getRol());
        String cedAnterior = resolverCedulaVinculo(anterior);
        if (eraDocente && !esDocente && cedAnterior != null) {
            docenteController.bajaLogicaCatalogoPorCedula(cedAnterior);
        }
    }

    private void aplicarVinculoTrasEliminar(Usuario eliminado) {
        if (omitirVinculo || docenteController == null || eliminado == null) {
            return;
        }
        if (esRolDocente(eliminado.getRol())) {
            String ced = resolverCedulaVinculo(eliminado);
            if (ced != null) {
                docenteController.bajaLogicaCatalogoPorCedula(ced);
            }
        }
    }

    private void cargarAdminPorDefecto() {
        Usuario admin = new Usuario();
        admin.setIdUsuario(secuenciaId);
        secuenciaId++;
        admin.setUsername("admin");
        admin.setPassword(PasswordUtil.hashPassword("12345678"));
        admin.setRol("ADMIN");
        admin.setEstado(1);
        usuarios.add(admin);
    }

    public Usuario registrar(Usuario usuario) {
        limpiarError();
        if (!validarReglasVinculoAlta(usuario)) {
            auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_CREATE, getUltimoError(), false);
            return null;
        }
        if (usuario.getPassword() != null) {
            usuario.setPassword(PasswordUtil.normalizarParaAlmacenar(usuario.getPassword()));
        }
        try {
            if (jdbc) {
                if (usuarioDAO.buscarPorUsername(usuario.getUsername()) != null) {
                    setUltimoError("El username ya está registrado.");
                    auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_CREATE,
                            "Alta usuario rechazada: username duplicado.", false);
                    return null;
                }
                Usuario creado = usuarioDAO.insertar(usuario);
                auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_CREATE,
                        "Usuario creado: " + creado.getUsername(), true);
                return creado;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_CREATE, ex.getMessage(), false);
            return null;
        }
        if (buscarPorUsername(usuario.getUsername()) != null) {
            setUltimoError("El username ya está registrado.");
            return null;
        }
        usuario.setIdUsuario(secuenciaId++);
        usuario.setEstado(1);
        usuarios.add(usuario);
        auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_CREATE,
                "Usuario creado: " + usuario.getUsername(), true);
        return usuario;
    }

    public boolean actualizar(Usuario usuarioActualizado) {
        limpiarError();
        Usuario existentePrevio = buscarPorId(usuarioActualizado.getIdUsuario());
        if (existentePrevio == null) {
            setUltimoError("Usuario no encontrado.");
            return false;
        }
        String pwdNueva = usuarioActualizado.getPassword();
        if (pwdNueva == null || pwdNueva.trim().isEmpty()) {
            usuarioActualizado.setPassword(existentePrevio.getPassword());
        } else {
            usuarioActualizado.setPassword(PasswordUtil.normalizarParaAlmacenar(pwdNueva.trim()));
        }
        if (!validarReglasVinculoCambio(existentePrevio, usuarioActualizado)) {
            auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_UPDATE, getUltimoError(), false);
            return false;
        }
        try {
            if (jdbc) {
                Usuario otro = usuarioDAO.buscarPorUsername(usuarioActualizado.getUsername());
                if (otro != null && otro.getIdUsuario() != usuarioActualizado.getIdUsuario()) {
                    setUltimoError("El username ya pertenece a otro usuario.");
                    auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_UPDATE, "Username duplicado.", false);
                    return false;
                }
                boolean ok = usuarioDAO.actualizar(usuarioActualizado);
                auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_UPDATE,
                        "Usuario id=" + usuarioActualizado.getIdUsuario(), ok);
                if (!ok) {
                    setUltimoError("No se pudo actualizar el usuario.");
                } else {
                    aplicarVinculoTrasPersistir(existentePrevio, usuarioActualizado);
                }
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_UPDATE, ex.getMessage(), false);
            return false;
        }
        Usuario existente = existentePrevio;
        Usuario otro = buscarPorUsername(usuarioActualizado.getUsername());
        if (otro != null && otro.getIdUsuario() != existente.getIdUsuario()) {
            setUltimoError("El username ya pertenece a otro usuario.");
            return false;
        }
        existente.setUsername(usuarioActualizado.getUsername());
        existente.setPassword(usuarioActualizado.getPassword());
        existente.setRol(usuarioActualizado.getRol());
        existente.setCedula(usuarioActualizado.getCedula());
        existente.setNombres(usuarioActualizado.getNombres());
        existente.setApellidos(usuarioActualizado.getApellidos());
        aplicarVinculoTrasPersistir(existentePrevio, existente);
        auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_UPDATE,
                "Usuario id=" + existente.getIdUsuario(), true);
        return true;
    }

    public Usuario buscarPorId(int idUsuario) {
        try {
            if (jdbc) {
                return usuarioDAO.buscarPorId(idUsuario);
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return null;
        }
        for (Usuario u : usuarios) {
            if (u.getIdUsuario() == idUsuario && u.getEstado() == 1) {
                return u;
            }
        }
        return null;
    }

    public Usuario buscarPorUsername(String username) {
        if (username == null) {
            return null;
        }
        try {
            if (jdbc) {
                return usuarioDAO.buscarPorUsername(username);
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return null;
        }
        for (Usuario u : usuarios) {
            if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username) && u.getEstado() == 1) {
                return u;
            }
        }
        return null;
    }

    public Usuario buscarPorUsernameInclInactivo(String username) {
        if (username == null) {
            return null;
        }
        try {
            if (jdbc) {
                return usuarioDAO.buscarPorUsernameInclInactivo(username);
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return null;
        }
        for (Usuario u : usuarios) {
            if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public boolean eliminarLogico(int idUsuario) {
        limpiarError();
        Usuario previo = buscarPorId(idUsuario);
        if (previo == null) {
            for (Usuario u : listarTodos()) {
                if (u.getIdUsuario() == idUsuario && u.getEstado() == 1) {
                    previo = u;
                    break;
                }
            }
        }
        try {
            if (jdbc) {
                boolean ok = usuarioDAO.eliminarLogico(idUsuario);
                if (ok && previo != null) {
                    aplicarVinculoTrasEliminar(previo);
                }
                auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_DELETE_LOGIC,
                        "Usuario id=" + idUsuario, ok);
                return ok;
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_DELETE_LOGIC, ex.getMessage(), false);
            return false;
        }
        Usuario existente = buscarPorIdMemoria(idUsuario);
        if (existente == null) {
            setUltimoError("Usuario no encontrado.");
            return false;
        }
        existente.setEstado(0);
        aplicarVinculoTrasEliminar(existente);
        auditarCrud(PermisosRol.MOD_USUARIOS, BitacoraController.ACCION_DELETE_LOGIC,
                "Usuario id=" + idUsuario, true);
        return true;
    }

    private Usuario buscarPorIdMemoria(int id) {
        for (Usuario u : usuarios) {
            if (u.getIdUsuario() == id) {
                return u;
            }
        }
        return null;
    }

    public List<Usuario> listarActivos() {
        try {
            if (jdbc) {
                return usuarioDAO.listarActivos();
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return new ArrayList<>();
        }
        List<Usuario> activos = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u.getEstado() == 1) {
                activos.add(u);
            }
        }
        return activos;
    }

    public List<Usuario> listarTodos() {
        try {
            if (jdbc) {
                return usuarioDAO.listarTodos();
            }
        } catch (SQLException ex) {
            setUltimoError(ex.getMessage());
            return new ArrayList<>();
        }
        List<Usuario> todos = new ArrayList<>(usuarios);
        todos.sort(java.util.Comparator.comparingInt(Usuario::getIdUsuario));
        return todos;
    }

    public int contarActivos() {
        try {
            if (jdbc) {
                return usuarioDAO.contarActivos();
            }
        } catch (SQLException ex) {
            return listarActivos().size();
        }
        return listarActivos().size();
    }

    /**
     * Actualiza el perfil del usuario en sesión (username, nombres, apellidos y opcionalmente clave).
     *
     * @return usuario actualizado o {@code null} si falló validación o persistencia
     */
    public Usuario actualizarPerfil(int idUsuario, String nuevoUsername, String nombres, String apellidos,
            String claveActual, String claveNueva, String claveConfirm) {
        limpiarError();
        Usuario existente = buscarPorId(idUsuario);
        if (existente == null) {
            setUltimoError("Usuario no encontrado.");
            return null;
        }
        String user = nuevoUsername != null ? nuevoUsername.trim() : "";
        if (user.isEmpty()) {
            setUltimoError("El username no puede estar vacío.");
            return null;
        }
        if (user.contains(" ")) {
            setUltimoError("El username no debe contener espacios.");
            return null;
        }
        String nom = nombres != null ? nombres.trim() : "";
        String ape = apellidos != null ? apellidos.trim() : "";
        if (!nom.isEmpty() && !nom.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            setUltimoError("Nombres inválidos: solo letras.");
            return null;
        }
        if (!ape.isEmpty() && !ape.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            setUltimoError("Apellidos inválidos: solo letras.");
            return null;
        }

        String passwordFinal = existente.getPassword();
        boolean cambiaClave = claveNueva != null && !claveNueva.trim().isEmpty();
        if (cambiaClave) {
            String actual = claveActual != null ? claveActual.trim() : "";
            String nueva = claveNueva.trim();
            String confirm = claveConfirm != null ? claveConfirm.trim() : "";
            if (actual.isEmpty()) {
                setUltimoError("Debe ingresar su clave actual para cambiarla.");
                return null;
            }
            if (!PasswordUtil.verificar(actual, existente.getPassword())) {
                setUltimoError("La clave actual no es correcta.");
                auditarCrud(PermisosRol.MOD_PERFIL, BitacoraController.ACCION_UPDATE, "Cambio de clave fallido.", false);
                return null;
            }
            if (nueva.isEmpty()) {
                setUltimoError("La clave nueva no puede estar vacía.");
                return null;
            }
            if (!nueva.equals(confirm)) {
                setUltimoError("La confirmación de la clave nueva no coincide.");
                return null;
            }
            passwordFinal = PasswordUtil.normalizarParaAlmacenar(nueva);
        }

        Usuario actualizado = new Usuario();
        actualizado.setIdUsuario(existente.getIdUsuario());
        actualizado.setUsername(user);
        actualizado.setPassword(passwordFinal);
        actualizado.setRol(existente.getRol());
        actualizado.setEstado(existente.getEstado());
        actualizado.setCedula(existente.getCedula());
        actualizado.setNombres(nom.isEmpty() ? existente.getNombres() : nom);
        actualizado.setApellidos(ape.isEmpty() ? existente.getApellidos() : ape);

        if ("DOCENTE".equalsIgnoreCase(existente.getRol()) && existente.getCedula() != null
                && !existente.getCedula().equals(user)) {
            setUltimoError("Para rol DOCENTE el username debe coincidir con la cédula.");
            return null;
        }

        boolean ok = actualizar(actualizado);
        if (!ok) {
            auditarCrud(PermisosRol.MOD_PERFIL, BitacoraController.ACCION_UPDATE,
                    "Perfil id=" + idUsuario + " — " + getUltimoError(), false);
            return null;
        }
        auditarCrud(PermisosRol.MOD_PERFIL, BitacoraController.ACCION_UPDATE,
                "Perfil actualizado: " + user + (cambiaClave ? " (clave)" : ""), true);
        return buscarPorId(idUsuario);
    }
}
