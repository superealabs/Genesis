export class RegisterDTO{
  name?: string
  email?:string
  password?:string
  roleidRoleEntity: {
    id?:string
  }

  constructor(name?:string, email?:string, password?:string, roleId?:string) {
    this.name = name
    this.email = email
    this.password = password
    this.roleidRoleEntity = {
      id: roleId
    }
  }
}
