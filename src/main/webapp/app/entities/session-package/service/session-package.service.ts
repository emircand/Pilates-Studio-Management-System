import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError, map } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { ISessionPackage, NewSessionPackage } from '../session-package.model';

export type PartialUpdateSessionPackage = Partial<ISessionPackage> & Pick<ISessionPackage, 'id'>;

type RestOf<T extends ISessionPackage | NewSessionPackage> = Omit<T, 'startDate' | 'endDate'> & {
  startDate?: string | null;
  endDate?: string | null;
};

export type RestSessionPackage = RestOf<ISessionPackage>;

export type NewRestSessionPackage = RestOf<NewSessionPackage>;

export type PartialUpdateRestSessionPackage = RestOf<PartialUpdateSessionPackage>;

export type EntityResponseType = HttpResponse<ISessionPackage>;
export type EntityArrayResponseType = HttpResponse<ISessionPackage[]>;

@Injectable({ providedIn: 'root' })
export class SessionPackageService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/session-packages');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/session-packages/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(sessionPackage: NewSessionPackage): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(sessionPackage);
    return this.http
      .post<RestSessionPackage>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(sessionPackage: ISessionPackage): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(sessionPackage);
    return this.http
      .put<RestSessionPackage>(`${this.resourceUrl}/${this.getSessionPackageIdentifier(sessionPackage)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(sessionPackage: PartialUpdateSessionPackage): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(sessionPackage);
    return this.http
      .patch<RestSessionPackage>(`${this.resourceUrl}/${this.getSessionPackageIdentifier(sessionPackage)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http
      .get<RestSessionPackage>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestSessionPackage[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestSessionPackage[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),
      catchError(() => scheduled([new HttpResponse<ISessionPackage[]>()], asapScheduler)),
    );
  }

  getSessionPackageIdentifier(sessionPackage: Pick<ISessionPackage, 'id'>): string {
    return sessionPackage.id;
  }

  compareSessionPackage(o1: Pick<ISessionPackage, 'id'> | null, o2: Pick<ISessionPackage, 'id'> | null): boolean {
    return o1 && o2 ? this.getSessionPackageIdentifier(o1) === this.getSessionPackageIdentifier(o2) : o1 === o2;
  }

  addSessionPackageToCollectionIfMissing<Type extends Pick<ISessionPackage, 'id'>>(
    sessionPackageCollection: Type[],
    ...sessionPackagesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const sessionPackages: Type[] = sessionPackagesToCheck.filter(isPresent);
    if (sessionPackages.length > 0) {
      const sessionPackageCollectionIdentifiers = sessionPackageCollection.map(
        sessionPackageItem => this.getSessionPackageIdentifier(sessionPackageItem)!,
      );
      const sessionPackagesToAdd = sessionPackages.filter(sessionPackageItem => {
        const sessionPackageIdentifier = this.getSessionPackageIdentifier(sessionPackageItem);
        if (sessionPackageCollectionIdentifiers.includes(sessionPackageIdentifier)) {
          return false;
        }
        sessionPackageCollectionIdentifiers.push(sessionPackageIdentifier);
        return true;
      });
      return [...sessionPackagesToAdd, ...sessionPackageCollection];
    }
    return sessionPackageCollection;
  }

  protected convertDateFromClient<T extends ISessionPackage | NewSessionPackage | PartialUpdateSessionPackage>(
    sessionPackage: T,
  ): RestOf<T> {
    return {
      ...sessionPackage,
      startDate: sessionPackage.startDate?.toJSON() ?? null,
      endDate: sessionPackage.endDate?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restSessionPackage: RestSessionPackage): ISessionPackage {
    return {
      ...restSessionPackage,
      startDate: restSessionPackage.startDate ? dayjs(restSessionPackage.startDate) : undefined,
      endDate: restSessionPackage.endDate ? dayjs(restSessionPackage.endDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestSessionPackage>): HttpResponse<ISessionPackage> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestSessionPackage[]>): HttpResponse<ISessionPackage[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
