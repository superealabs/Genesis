export interface IRole{
  id?:string,
  name?:string
}

export class Role implements IRole {
  id?:string
  name?:string

  constructor(data?: Partial<IRole>) {
    this.id = data?.id
    this.name = data?.name
  }
}
