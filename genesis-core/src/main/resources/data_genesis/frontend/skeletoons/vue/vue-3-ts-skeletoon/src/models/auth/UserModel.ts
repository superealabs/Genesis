export interface IUser {
  id?: number
  username?: string
  email?: string
  authorities?: [
    {
      authority: string
    },
  ]
  enabled?: boolean
  accountNonExpired?: boolean
  accountNonLocked?: boolean
  credentialsNonExpired?: boolean
}

export class User implements IUser {
  id?: number
  username?: string
  email?: string
  authorities?: [
    {
      authority: string
    },
  ]
  enabled?: boolean
  accountNonExpired?: boolean
  accountNonLocked?: boolean
  credentialsNonExpired?: boolean

  constructor(data?: Partial<IUser>) {
    this.id = data?.id
    this.username = data?.username
    this.email = data?.email
    this.authorities = data?.authorities
    this.enabled = data?.enabled
    this.accountNonExpired = data?.accountNonExpired
    this.accountNonLocked = data?.accountNonLocked
    this.credentialsNonExpired = data?.credentialsNonExpired
  }

  static voidUser(): User {
    return new User({
      id: undefined,
      username: undefined,
      email: undefined,
      authorities: undefined,
      enabled: undefined,
      accountNonExpired: undefined,
      accountNonLocked: undefined,
      credentialsNonExpired: undefined,
    })
  }
}
