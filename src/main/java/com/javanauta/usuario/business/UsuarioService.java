package com.javanauta.usuario.business;
import com.javanauta.usuario.business.converter.UsuarioConverter;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.exeptions.ConflictExeption;
import com.javanauta.usuario.infrastructure.exeptions.ResourceNotFoundException;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructure.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.BcryptPassword4jPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsusario(usuarioDTO);
        return usuarioConverter.paraUsusarioDTO(usuarioRepository.save(usuario));

    }

    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if(existe){
                throw new ConflictExeption("Email já cadastrado" + email);
            }
        }catch (ConflictExeption e){
            throw new ConflictExeption("Email já cadastrado" + e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email){

        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
                ()->new ResourceNotFoundException("Email não encontrado" + email));

    }

    public void deletaUsuarioPorEmail( String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){
        //Aqui buscamos o email do usuário através do token(para tirar a obrigatoriedade do email).
        String email = jwtUtil.extrairEmailToken( token.substring(7));

        //Criptografia de senha.
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()): null);

        //Busca os dados do usuário no banco de dados.
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não locolizado"));

        //Mesclou os dados que recebemos na requisição DTO com os dados do banco de dados.
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

        // Salvou os dados do susuário convertido e depois pegou o retorno e converteu usuárioDTO.
        return usuarioConverter.paraUsusarioDTO(usuarioRepository.save(usuario));

    }
}
