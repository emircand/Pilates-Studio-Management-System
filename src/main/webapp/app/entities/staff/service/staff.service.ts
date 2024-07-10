import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError, map } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IStaff, NewStaff } from '../staff.model';

export type PartialUpdateStaff = Partial<IStaff> & Pick<IStaff, 'id'>;

type RestOf<T extends IStaff | NewStaff> = Omit<T, 'birthday' | 'hireDate' | 'role'> & {
  birthday?: string | null;
  hireDate?: string | null;
  role?: string | null;
};

export type RestStaff = RestOf<IStaff>;

export type NewRestStaff = RestOf<NewStaff>;

export type PartialUpdateRestStaff = RestOf<PartialUpdateStaff>;

export type EntityResponseType = HttpResponse<IStaff>;
export type EntityArrayResponseType = HttpResponse<IStaff[]>;

@Injectable({ providedIn: 'root' })
export class StaffService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/staff');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/staff/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(staff: NewStaff): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(staff);
    return this.http.post<RestStaff>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(staff: IStaff): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(staff);
    return this.http
      .put<RestStaff>(`${this.resourceUrl}/${this.getStaffIdentifier(staff)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(staff: PartialUpdateStaff): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(staff);
    return this.http
      .patch<RestStaff>(`${this.resourceUrl}/${this.getStaffIdentifier(staff)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestStaff>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestStaff[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestStaff[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),
      catchError(() => scheduled([new HttpResponse<IStaff[]>()], asapScheduler)),
    );
  }

  getStaffIdentifier(staff: Pick<IStaff, 'id'>): number {
    return staff.id;
  }

  compareStaff(o1: Pick<IStaff, 'id'> | null, o2: Pick<IStaff, 'id'> | null): boolean {
    return o1 && o2 ? this.getStaffIdentifier(o1) === this.getStaffIdentifier(o2) : o1 === o2;
  }

  addStaffToCollectionIfMissing<Type extends Pick<IStaff, 'id'>>(
    staffCollection: Type[],
    ...staffToCheck: (Type | null | undefined)[]
  ): Type[] {
    const staff: Type[] = staffToCheck.filter(isPresent);
    if (staff.length > 0) {
      const staffCollectionIdentifiers = staffCollection.map(staffItem => this.getStaffIdentifier(staffItem)!);
      const staffToAdd = staff.filter(staffItem => {
        const staffIdentifier = this.getStaffIdentifier(staffItem);
        if (staffCollectionIdentifiers.includes(staffIdentifier)) {
          return false;
        }
        staffCollectionIdentifiers.push(staffIdentifier);
        return true;
      });
      return [...staffToAdd, ...staffCollection];
    }
    return staffCollection;
  }

  protected convertDateFromClient<T extends IStaff | NewStaff | PartialUpdateStaff>(staff: T): RestOf<T> {
    return {
      ...staff,
      birthday: staff.birthday?.toJSON() ?? null,
      hireDate: staff.hireDate?.toJSON() ?? null,
      role: staff.role?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restStaff: RestStaff): IStaff {
    return {
      ...restStaff,
      birthday: restStaff.birthday ? dayjs(restStaff.birthday) : undefined,
      hireDate: restStaff.hireDate ? dayjs(restStaff.hireDate) : undefined,
      role: restStaff.role ? dayjs(restStaff.role) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestStaff>): HttpResponse<IStaff> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestStaff[]>): HttpResponse<IStaff[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
